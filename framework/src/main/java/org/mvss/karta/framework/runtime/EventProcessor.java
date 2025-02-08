package org.mvss.karta.framework.runtime;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.dependencyinjection.annotations.PropertyMapping;
import org.mvss.karta.framework.models.event.Event;
import org.mvss.karta.framework.models.event.TestIncidentOccurrenceEvent;
import org.mvss.karta.framework.models.result.ScenarioResult;
import org.mvss.karta.framework.models.test.PreparedScenario;
import org.mvss.karta.framework.models.test.TestFeature;
import org.mvss.karta.framework.models.test.TestIncident;
import org.mvss.karta.framework.plugins.TestEventListener;
import org.mvss.karta.framework.plugins.TestLifeCycleHook;
import org.mvss.karta.framework.threading.BlockingRunnableQueue;
import org.mvss.karta.framework.utils.WaitUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Log4j2
@SuppressWarnings("unused")
public class EventProcessor implements AutoCloseable {
    private static final long POLL_TIME_FOR_EVENT_QUEUE_CLEARING = 1000;
    private final HashSet<TestLifeCycleHook> lifeCycleHooks = new HashSet<>();
    private final HashSet<TestEventListener> testEventListeners = new HashSet<>();
    @Getter
    @Setter
    // TODO: Events sent out of order due to multiple threads. Need a fix.
    @PropertyMapping("EventProcessor.numberOfThread")
    private int numberOfThread = 1;
    @Getter
    @Setter
    @PropertyMapping("EventProcessor.maxEventQueueSize")
    private int maxEventQueueSize = 100;
    private ExecutorService eventListenerExecutorService = null;
    private BlockingRunnableQueue eventProcessingQueue;

    private final ThreadFactory threadFactory;

    public EventProcessor(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    public boolean addEventListener(TestEventListener testEventListener) {
        return testEventListeners.add(testEventListener);
    }

    public boolean removeEventListener(TestEventListener testEventListener) {
        return testEventListeners.remove(testEventListener);
    }

    public boolean addLifeCycleHook(TestLifeCycleHook testEventListener) {
        return lifeCycleHooks.add(testEventListener);
    }

    public boolean removeLifeCycleHook(TestLifeCycleHook testEventListener) {
        return lifeCycleHooks.remove(testEventListener);
    }

    public void start() {
        // TODO: Change to thread factory to be able to manage threads.
        eventProcessingQueue = new BlockingRunnableQueue(maxEventQueueSize);
        eventListenerExecutorService = new ThreadPoolExecutor(numberOfThread, numberOfThread, 0L, TimeUnit.MILLISECONDS, eventProcessingQueue, threadFactory);
    }

    @Override
    public void close() {
        try {
            while (!eventProcessingQueue.isEmpty()) {
                if (WaitUtil.sleep(POLL_TIME_FOR_EVENT_QUEUE_CLEARING, false, false)) {
                    log.info("Interrupted while trying to wait for event queue clearing");
                    break;
                }
            }
            eventListenerExecutorService.shutdown();
            if (!eventListenerExecutorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                log.error("Failed to wait for event listeners execution service");
            }
        } catch (Throwable t) {
            log.error("Exception occurred while stopping event processor ", t);
        }
    }

    private void sendEventsToListeners(Event event) {
        for (TestEventListener testEventListener : testEventListeners) {
            try {
                eventListenerExecutorService.submit(() -> testEventListener.processEvent(event));
            } catch (Throwable t) {
                log.error("Exception occurred during event processing ", t);
            }
        }
    }

    public void raiseEvent(Event event) {
        sendEventsToListeners(event);
    }

    public void fail(String runName, String featureName, Long iterationIndex, String scenarioName, String stepIdentifier, TestIncident incident)
            throws TestFailureException {
        if (incident != null) {
            raiseEvent(new TestIncidentOccurrenceEvent(runName, featureName, iterationIndex, scenarioName, stepIdentifier, incident));
            throw new TestFailureException(incident.getMessage(), incident.getThrownCause());
        }
    }

    public void raiseIncident(String runName, String featureName, Long iterationIndex, String scenarioName, String stepIdentifier,
                              TestIncident incident) {
        if (incident != null) {
            raiseEvent(new TestIncidentOccurrenceEvent(runName, featureName, iterationIndex, scenarioName, stepIdentifier, incident));
        }
    }

    public boolean runStart(String runName, ArrayList<String> tags) {
        boolean success = true;

        for (TestLifeCycleHook lifeCycleHook : lifeCycleHooks) {
            success = success && lifeCycleHook.runStart(runName, tags);
        }

        return success;
    }

    public boolean featureStart(String runName, TestFeature feature, ArrayList<String> tags) {
        boolean success = true;

        if (tags != null) {
            for (TestLifeCycleHook lifeCycleHook : lifeCycleHooks) {
                success = success && lifeCycleHook.featureStart(runName, feature, tags);
            }
        }

        return success;
    }

    public boolean scenarioStart(String runName, String featureName, PreparedScenario scenario, ArrayList<String> tags) {
        boolean success = true;

        if (tags != null) {
            for (TestLifeCycleHook lifeCycleHook : lifeCycleHooks) {
                success = success && lifeCycleHook.scenarioStart(runName, featureName, scenario, tags);
            }
        }

        return success;
    }

    public boolean scenarioStop(String runName, String featureName, PreparedScenario scenario, ArrayList<String> tags) {
        boolean success = true;

        if (tags != null) {
            for (TestLifeCycleHook lifeCycleHook : lifeCycleHooks) {
                success = success && lifeCycleHook.scenarioStop(runName, featureName, scenario, tags);
            }
        }

        return success;
    }

    public boolean scenarioFailed(String runName, String featureName, PreparedScenario scenario, ArrayList<String> tags, ScenarioResult scenarioResult) {
        boolean success = true;

        if (tags != null) {
            for (TestLifeCycleHook lifeCycleHook : lifeCycleHooks) {
                success = success && lifeCycleHook.scenarioFailed(runName, featureName, scenario, tags, scenarioResult);
            }
        }

        return success;
    }

    public boolean featureStop(String runName, TestFeature feature, ArrayList<String> tags) {
        boolean success = true;

        if (tags != null) {
            for (TestLifeCycleHook lifeCycleHook : lifeCycleHooks) {
                success = success && lifeCycleHook.featureStop(runName, feature, tags);
            }
        }

        return success;
    }

    public boolean runStop(String runName, ArrayList<String> tags) {
        boolean success = true;

        for (TestLifeCycleHook lifeCycleHook : lifeCycleHooks) {
            success = success && lifeCycleHook.runStop(runName, tags);
        }

        return success;
    }
}
