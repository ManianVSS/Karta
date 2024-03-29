package org.mvss.karta.framework.runtime;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.dependencyinjection.TestProperties;
import org.mvss.karta.dependencyinjection.utils.DataUtils;
import org.mvss.karta.framework.models.event.ScenarioCompleteEvent;
import org.mvss.karta.framework.models.event.ScenarioStartEvent;
import org.mvss.karta.framework.models.result.ScenarioResult;
import org.mvss.karta.framework.models.run.RunInfo;
import org.mvss.karta.framework.models.test.PreparedScenario;
import org.mvss.karta.framework.models.test.TestScenario;
import org.mvss.karta.framework.models.test.TestStep;
import org.mvss.karta.framework.nodes.KartaNode;
import org.mvss.karta.framework.threading.BlockingRunnableQueue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
@Builder
public class IterationRunner implements Callable<HashMap<String, ScenarioResult>> {
    private KartaRuntime kartaRuntime;
    private RunInfo runInfo;
    private String featureName;

    private int iterationIndex;

    private TestProperties testProperties;
    private HashMap<String, ArrayList<Serializable>> commonTestDataSet;
    private ArrayList<TestStep> scenarioSetupSteps;
    private ArrayList<TestScenario> scenariosToRun;
    private ArrayList<TestStep> scenarioTearDownSteps;

    @Builder.Default
    private KartaNode minionToUse = null;

    private HashMap<TestScenario, AtomicInteger> scenarioIterationIndexMap;

    @Builder.Default
    private HashMap<String, Serializable> variables = new HashMap<>();

    private HashMap<String, ScenarioResult> result;

    private Consumer<HashMap<String, ScenarioResult>> resultConsumer;

    @Builder.Default
    private HashMap<PreparedScenario, TestScenario> scenarioMapping = new HashMap<>();

    /**
     * The callback implementation for scenario result updates for running test scenarios
     */
    private synchronized void accumulateScenarioResult(PreparedScenario scenario, ScenarioResult scenarioResult) {
        result.put(scenario.getName(), kartaRuntime.getKartaConfiguration().getDetailedReport() ? scenarioResult : scenarioResult.trimForReport());

        kartaRuntime.getEventProcessor().raiseEvent(new ScenarioCompleteEvent(runInfo.getRunName(), featureName, iterationIndex, scenarioMapping.get(scenario), scenarioResult));
    }

    @Override
    public HashMap<String, ScenarioResult> call() throws InterruptedException {
        result = new HashMap<>();
        try {
            String runName = runInfo.getRunName();

            EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
            log.debug("Iteration " + iterationIndex + " with scenarios " + scenariosToRun);

            ExecutorService scenarioExecutionService = null;
            boolean runScenarioParallely = runInfo.isRunAllScenarioParallely();

            if (runScenarioParallely) {
                int numberOfScenarios = scenariosToRun.size();
                scenarioExecutionService = new ThreadPoolExecutor(numberOfScenarios, numberOfScenarios, 0L, TimeUnit.MILLISECONDS, new BlockingRunnableQueue(numberOfScenarios), kartaRuntime.getThreadFactory());
            }

            for (TestScenario testScenario : scenariosToRun) {
                int scenarioIterationNumber = ((scenarioIterationIndexMap != null) && (scenarioIterationIndexMap.containsKey(testScenario))) ? scenarioIterationIndexMap.get(testScenario).getAndIncrement() : 0;
                log.debug("Running Scenario: " + testScenario.getName() + "[" + scenarioIterationNumber + "]:");

                PreparedScenario preparedScenario = kartaRuntime.getPreparedScenario(runInfo, featureName, scenarioIterationNumber, DataUtils.cloneMap(variables), commonTestDataSet, scenarioSetupSteps, testProperties, testScenario, scenarioTearDownSteps);
                scenarioMapping.put(preparedScenario, testScenario);

                eventProcessor.raiseEvent(new ScenarioStartEvent(runName, featureName, iterationIndex, testScenario));

                ScenarioRunner scenarioRunner = ScenarioRunner.builder().kartaRuntime(kartaRuntime).runInfo(runInfo).featureName(featureName).iterationIndex(iterationIndex).testScenario(preparedScenario).scenarioIterationNumber(scenarioIterationNumber).minionToUse(minionToUse).resultConsumer(this::accumulateScenarioResult).build();

                if (runScenarioParallely) {
                    scenarioExecutionService.submit(scenarioRunner);
                } else {
                    scenarioRunner.call();
                }

            }

            if (runScenarioParallely) {
                scenarioExecutionService.shutdown();
                if (!scenarioExecutionService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                    log.error("Failed to wait for scenario executor service shutdown");
                }
            }

        } catch (InterruptedException ie) {
            throw ie;
        } catch (Throwable t) {
            log.error("Error when running iteration: ", t);
        }

        if (resultConsumer != null) {
            resultConsumer.accept(result);
        }

        return result;
    }
}
