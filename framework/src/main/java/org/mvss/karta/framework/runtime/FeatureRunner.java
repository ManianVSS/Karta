package org.mvss.karta.framework.runtime;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.BeanRegistry;
import org.mvss.karta.dependencyinjection.TestProperties;
import org.mvss.karta.dependencyinjection.utils.DataUtils;
import org.mvss.karta.framework.models.event.*;
import org.mvss.karta.framework.models.generic.SerializableKVP;
import org.mvss.karta.framework.models.result.FeatureResult;
import org.mvss.karta.framework.models.result.ScenarioResult;
import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.models.run.RunInfo;
import org.mvss.karta.framework.models.test.*;
import org.mvss.karta.framework.nodes.IKartaNodeRegistry;
import org.mvss.karta.framework.nodes.KartaNode;
import org.mvss.karta.framework.threading.BlockingRunnableQueue;
import org.mvss.karta.framework.utils.RandomizationUtils;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Runner class to run TestFeatures for Karta
 *
 * @author Manian
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
@Builder
public class FeatureRunner implements Callable<FeatureResult> {
    private KartaRuntime kartaRuntime;

    private RunInfo runInfo;

    private TestProperties testProperties;

    private TestFeature testFeature;

    /**
     * The call back for updating execution result of the TestFeature after run completes.
     */
    private Consumer<FeatureResult> resultConsumer;

    private FeatureResult result;
    @Builder.Default
    private ArrayList<Long> runningJobs = new ArrayList<>();
    @Builder.Default
    private ArrayList<Thread> daemonJobThreads = new ArrayList<>();

    /**
     * The callback implementation for feature iteration result updates for running Test Feature
     *
     * @param iterationResult HashMap<String, ScenarioResult>
     */
    private void accumulateIterationResult(HashMap<String, ScenarioResult> iterationResult) {
        result.addIterationResult(iterationResult, kartaRuntime.getKartaConfiguration().getDetailedReport());
    }

    private void deleteJobs() {
        boolean deleteJobResults = QuartzJobScheduler.deleteJobs(runningJobs);

        for (Thread daemonJobThread : daemonJobThreads) {
            try {
                daemonJobThread.interrupt();
            } catch (SecurityException se) {
                log.error("Error while interrupting daemon job thread " + daemonJobThread.getName(), se);
                deleteJobResults = false;
            }
        }

        if (!deleteJobResults) {
            log.error("Failed to delete test jobs");
            result.setSuccessful(false);
        }
    }

    public void updateResultCallBack() {
        result.setEndTime(new Date());
        if (resultConsumer != null) {
            resultConsumer.accept(result);
        }
    }

    /**
     * After initializing calling this method would run the feature.
     * Call implementation for asynchronous calling.
     */
    @Override
    public FeatureResult call() throws InterruptedException {
        try {
            String runName = runInfo.getRunName();
            ArrayList<String> tags = runInfo.getTags();

            result = new FeatureResult();
            result.setFeatureName(testFeature.getName());
            EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
            IKartaNodeRegistry nodeRegistry = kartaRuntime.getNodeRegistry();
            BeanRegistry contextBeanRegistry = new BeanRegistry();
            contextBeanRegistry.put(testProperties);

            Random random = kartaRuntime.getRandom();

            boolean useMinions = kartaRuntime.getKartaConfiguration().getMinionsEnabled() && !nodeRegistry.getMinions().isEmpty();

            eventProcessor.raiseEvent(new FeatureStartEvent(runName, testFeature));

            if (tags != null) {
                if (!eventProcessor.featureStart(runName, testFeature, tags)) {
                    eventProcessor.featureStop(runName, testFeature, tags);
                    result.setError(true);
                    updateResultCallBack();
                    return result;
                }
            }

            HashMap<String, Serializable> variables = new HashMap<>();

            for (TestJob job : testFeature.getTestJobs()) {
                try {
                    long jobInterval = job.getInterval();
                    int repeatCount = job.getIterationCount();

                    if (job.isDaemonProcess()) {
                        DaemonTestJob daemonTestJob = DaemonTestJob.builder().kartaRuntime(kartaRuntime).runInfo(runInfo).featureName(testFeature.getName()).testProperties(testProperties).testJob(job).contextBeanRegistry(contextBeanRegistry).build();
                        Thread daemonJobThread = kartaRuntime.getThreadFactory().newThread(daemonTestJob);
                        daemonJobThread.start();
                        daemonJobThreads.add(daemonJobThread);
                    } else if (jobInterval > 0) {
                        HashMap<String, Object> jobData = new HashMap<>();
                        jobData.put(Constants.KARTA_RUNTIME, kartaRuntime);
                        jobData.put(Constants.RUN_INFO, runInfo);
                        jobData.put(Constants.FEATURE_NAME, testFeature.getName());
                        jobData.put(Constants.TEST_PROPERTIES, testProperties);
                        jobData.put(Constants.TEST_JOB, job);
                        jobData.put(Constants.ITERATION_COUNTER, new AtomicInteger());
                        jobData.put(Constants.BEAN_REGISTRY, contextBeanRegistry);
                        long jobId = QuartzJobScheduler.scheduleJob(QuartzTestJob.class, jobInterval, repeatCount, jobData);
                        runningJobs.add(jobId);
                    } else {
                        TestJobRunner.run(kartaRuntime, runInfo, testFeature.getName(), testProperties, job, 0, contextBeanRegistry);
                    }
                } catch (Throwable t) {
                    log.error("Exception occurred while scheduling jobs ", t);
                    deleteJobs();
                    throw t;
                }
            }

            int iterationIndex = -1;

            HashMap<TestScenario, AtomicInteger> scenarioIterationIndexMap = new HashMap<>();
            testFeature.getTestScenarios().forEach((scenario) -> scenarioIterationIndexMap.put(scenario, new AtomicInteger()));

            long setupStepIndex = -1;
            for (TestStep step : testFeature.getSetupSteps()) {
                setupStepIndex++;
                StepResult stepResult = new StepResult();
                stepResult.setStepIndex(setupStepIndex);
                stepResult.setSuccessful(true);

                ArrayList<PreparedStep> preparedSteps = kartaRuntime.getPreparedStep(runInfo, testFeature.getName(), iterationIndex, Constants.__FEATURE_SETUP__, variables, testFeature.getTestDataSet(), testProperties, step, contextBeanRegistry);


                for (PreparedStep preparedStep : preparedSteps) {
                    if (kartaRuntime.shouldStepNeedNotBeRun(runInfo, preparedStep)) {
                        continue;
                    }

                    eventProcessor.raiseEvent(new FeatureSetupStepStartEvent(runName, testFeature, preparedStep));

                    try {
                        stepResult = kartaRuntime.runStep(runInfo, preparedStep);
                        stepResult.setStepIndex(setupStepIndex);
                    } catch (TestFailureException tfe) {
                        log.error("Exception when running step", tfe);
                        stepResult.setSuccessful(false);
                        TestIncident incident = TestIncident.builder().thrownCause(tfe).build();
                        stepResult.addIncident(incident);
                    } finally {
                        eventProcessor.raiseEvent(new FeatureSetupStepCompleteEvent(runName, testFeature, preparedStep, stepResult));

                        result.getSetupResults().add(new SerializableKVP<>(step.getStep(), stepResult));
                        result.getIncidents().addAll(stepResult.getIncidents());

                        if (!stepResult.isPassed()) {
                            result.setSuccessful(false);
                            deleteJobs();

                            eventProcessor.raiseEvent(new FeatureCompleteEvent(runName, testFeature, result));

                            if (tags != null) {
                                if (!eventProcessor.featureStop(runName, testFeature, tags)) {
                                    result.setError(true);
                                }
                            }
                            updateResultCallBack();
                        }
                    }


                    if (!result.isSuccessful()) {
                        return result;
                    }
                }
            }

            long numberOfIterations = runInfo.getNumberOfIterations();
            int numberOfIterationsInParallel = runInfo.getNumberOfIterationsInParallel();
            boolean chanceBasedScenarioExecution = runInfo.isChanceBasedScenarioExecution();
            boolean exclusiveScenarioPerIteration = runInfo.isExclusiveScenarioPerIteration();
            Duration targetRunDuration = runInfo.getRunDuration();
            Duration coolDownBetweenIterations = runInfo.getCoolDownBetweenIterations();
            long iterationsPerCoolDownPeriod = runInfo.getIterationsPerCoolDownPeriod();

            if (!DataUtils.inRange(numberOfIterations, 0, Integer.MAX_VALUE)) {
                log.error("Configuration error: invalid number of iterations: " + numberOfIterations);
                numberOfIterations = 0;
            }

            if (!DataUtils.inRange(numberOfIterationsInParallel, 1, Integer.MAX_VALUE)) {
                log.error("Configuration error: invalid number of threads: " + numberOfIterationsInParallel);
                numberOfIterationsInParallel = 1;
            }

            ExecutorService iterationExecutionService = null;

            if (numberOfIterationsInParallel > 1) {
                iterationExecutionService = new ThreadPoolExecutor(numberOfIterationsInParallel, numberOfIterationsInParallel, 0L, TimeUnit.MILLISECONDS, new BlockingRunnableQueue(numberOfIterationsInParallel), kartaRuntime.getThreadFactory());
            }

            Instant startTime = Instant.now();

            for (iterationIndex = 0; (numberOfIterations <= 0) || (iterationIndex < numberOfIterations); iterationIndex++) {
                // Break on target Run Duration
                if (targetRunDuration != null) {
                    if (targetRunDuration.compareTo(Duration.between(startTime, Instant.now())) <= 0) {
                        break;
                    }
                }

                ArrayList<TestScenario> scenariosToRun = new ArrayList<>();

                if (chanceBasedScenarioExecution) {
                    if (exclusiveScenarioPerIteration) {
                        TestScenario scenarioToRun = RandomizationUtils.generateNextMutexComposition(random, testFeature.getTestScenarios());

                        if (scenarioToRun != null) {
                            scenariosToRun.add(scenarioToRun);
                        } else {
                            continue;
                        }
                    } else {
                        scenariosToRun.addAll(RandomizationUtils.generateNextComposition(random, testFeature.getTestScenarios()));
                    }
                } else {
                    scenariosToRun = testFeature.getTestScenarios();
                }

                IterationRunner iterationRunner = IterationRunner.builder().kartaRuntime(kartaRuntime).runInfo(runInfo).featureName(testFeature.getName()).testProperties(testProperties).commonTestDataSet(testFeature.getTestDataSet()).scenarioSetupSteps(testFeature.getScenarioSetupSteps()).scenariosToRun(scenariosToRun).scenarioTearDownSteps(testFeature.getScenarioTearDownSteps()).iterationIndex(iterationIndex).scenarioIterationIndexMap(scenarioIterationIndexMap).variables(DataUtils.cloneMap(variables)).resultConsumer(this::accumulateIterationResult).build();

                if (useMinions) {
                    KartaNode minion = nodeRegistry.getNextMinion();

                    if (minion != null) {
                        iterationRunner.setMinionToUse(minion);
                    }
                }

                if (numberOfIterationsInParallel == 1) {
                    log.debug("Iteration start " + iterationIndex + " with scenarios " + scenariosToRun);
                    iterationRunner.call();
                } else {
                    log.debug("Iteration queued " + iterationIndex + " with scenarios " + scenariosToRun);
                    assert iterationExecutionService != null;
                    iterationExecutionService.submit(iterationRunner);
                }

                if (coolDownBetweenIterations != null) {
                    if ((iterationIndex + 1) % (numberOfIterationsInParallel * iterationsPerCoolDownPeriod) == 0) {
                        Thread.sleep(coolDownBetweenIterations.toMillis());
                    }
                }
            }

            if (numberOfIterationsInParallel > 1) {
                iterationExecutionService.shutdown();
                if (!iterationExecutionService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                    iterationExecutionService.shutdownNow();
                }
            }

            testFeature.getTestScenarios().forEach((scenario) -> scenarioIterationIndexMap.get(scenario).set(0));

            long teardownStepIndex = -1;
            iterationIndex = -1;
            for (TestStep step : testFeature.getTearDownSteps()) {
                teardownStepIndex++;
                StepResult stepResult = new StepResult();
                stepResult.setStepIndex(teardownStepIndex);
                ArrayList<PreparedStep> preparedSteps = kartaRuntime.getPreparedStep(runInfo, testFeature.getName(), iterationIndex, Constants.__FEATURE_TEARDOWN__, variables, testFeature.getTestDataSet(), testProperties, step, contextBeanRegistry);

                for (PreparedStep preparedStep : preparedSteps) {
                    if (kartaRuntime.shouldStepNeedNotBeRun(runInfo, preparedStep)) {
                        continue;
                    }

                    eventProcessor.raiseEvent(new FeatureTearDownStepStartEvent(runName, testFeature, preparedStep));

                    try {
                        stepResult = kartaRuntime.runStep(runInfo, preparedStep);
                        stepResult.setStepIndex(teardownStepIndex);
                    } catch (TestFailureException tfe) {
                        log.error("Exception when running step", tfe);
                        stepResult.setSuccessful(false);
                        TestIncident incident = TestIncident.builder().thrownCause(tfe).build();
                        stepResult.addIncident(incident);
                    } finally {
                        eventProcessor.raiseEvent(new FeatureTearDownStepCompleteEvent(runName, testFeature, preparedStep, stepResult));

                        result.getTearDownResults().add(new SerializableKVP<>(step.getStep(), stepResult));
                        result.getIncidents().addAll(stepResult.getIncidents());

                        if (!stepResult.isPassed()) {
                            result.setSuccessful(false);
                        }
                    }
                }
            }

            deleteJobs();

            if (tags != null) {
                if (!eventProcessor.featureStop(runName, testFeature, tags)) {
                    result.setError(true);
                }
            }

            eventProcessor.raiseEvent(new FeatureCompleteEvent(runName, testFeature, result));
        } catch (InterruptedException ie) {
            throw ie;
        } catch (Throwable t) {
            log.error("Exception occurred during feature run", t);
            log.error(ExceptionUtils.getStackTrace(t));
            result.setError(true);
        }

        updateResultCallBack();
        return result;
    }
}
