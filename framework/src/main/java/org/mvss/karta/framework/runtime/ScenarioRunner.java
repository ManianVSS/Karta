package org.mvss.karta.framework.runtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mvss.karta.framework.models.event.*;
import org.mvss.karta.framework.models.generic.SerializableKVP;
import org.mvss.karta.framework.models.result.ScenarioResult;
import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.models.run.RunInfo;
import org.mvss.karta.framework.models.test.PreparedChaosAction;
import org.mvss.karta.framework.models.test.PreparedScenario;
import org.mvss.karta.framework.models.test.PreparedStep;
import org.mvss.karta.framework.models.test.TestIncident;
import org.mvss.karta.framework.nodes.KartaNode;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
@JsonInclude(value = Include.NON_ABSENT, content = Include.NON_ABSENT)
@Builder
public class ScenarioRunner implements Callable<ScenarioResult> {
    private static final String SCENARIO_FAILURE_HOOKS_PROCESSING_FAILED = "Scenario failure hooks processing failed.";

    private KartaRuntime kartaRuntime;
    private RunInfo runInfo;

    private String featureName;
    private int iterationIndex;

    private PreparedScenario testScenario;

    private long scenarioIterationNumber;

    private ScenarioResult result;

    @Builder.Default
    private KartaNode minionToUse = null;

    private BiConsumer<PreparedScenario, ScenarioResult> resultConsumer;

    public void updateResultCallBack() {
        if (resultConsumer != null) {
            resultConsumer.accept(testScenario, result);
        }
    }

    @Override
    public ScenarioResult call() throws InterruptedException {
        // TODO: Check for nulls

        if (minionToUse == null) {
            String runName = runInfo.getRunName();

            result = new ScenarioResult();
            result.setIterationIndex(iterationIndex);

            EventProcessor eventProcessor = kartaRuntime.getEventProcessor();

            // This should run at scenario runner since this need to run on the node where scenario is to be run
            testScenario.propagateContextBeanRegistry();

            ArrayList<String> tags = runInfo.getTags();
            if (tags != null) {
                if (!eventProcessor.scenarioStart(runName, featureName, testScenario, tags)) {
                    eventProcessor.scenarioStop(runName, featureName, testScenario, tags);
                    result.setError(true);
                    updateResultCallBack();
                    return result;
                }
            }

            log.debug("Running Scenario: " + testScenario);

            try {
                long setupStepIndex = 0;
                for (PreparedStep step : testScenario.getSetupSteps()) {
                    if (kartaRuntime.shouldStepNeedNotBeRun(runInfo, step)) {
                        continue;
                    }

                    eventProcessor.raiseEvent(new ScenarioSetupStepStartEvent(runName, featureName, iterationIndex, testScenario.getName(), step));
                    StepResult stepResult = kartaRuntime.runStep(runInfo, step);
                    stepResult.setStepIndex(setupStepIndex++);
                    eventProcessor.raiseEvent(
                            new ScenarioSetupStepCompleteEvent(runName, featureName, iterationIndex, testScenario.getName(), step, stepResult));
                    result.getSetupResults().add(new SerializableKVP<>(step.getIdentifier(), stepResult));
                    result.getIncidents().addAll(stepResult.getIncidents());

                    if (!stepResult.isPassed()) {
                        result.setSuccessful(false);
                        if ((tags != null) && !eventProcessor.scenarioFailed(runName, runName, testScenario, tags, result)) {
                            log.error(SCENARIO_FAILURE_HOOKS_PROCESSING_FAILED);
                        }
                        break;
                    }

                }

                if (result.isSuccessful()) {
                    long chaosStepIndex = 0;
                    for (PreparedChaosAction preparedChaosAction : testScenario.getChaosActions()) {
                        eventProcessor.raiseEvent(
                                new ScenarioChaosActionStartEvent(runName, featureName, iterationIndex, testScenario.getName(), preparedChaosAction));
                        StepResult stepResult = kartaRuntime.runChaosAction(runInfo, preparedChaosAction);
                        stepResult.setStepIndex(chaosStepIndex++);
                        eventProcessor.raiseEvent(
                                new ScenarioChaosActionCompleteEvent(runName, featureName, iterationIndex, testScenario.getName(), preparedChaosAction,
                                        stepResult));
                        result.getChaosActionResults().add(new SerializableKVP<>(preparedChaosAction.getName(), stepResult));
                        result.getIncidents().addAll(stepResult.getIncidents());

                        if (!stepResult.isPassed()) {
                            result.setSuccessful(false);
                            if ((tags != null) && !eventProcessor.scenarioFailed(runName, runName, testScenario, tags, result)) {
                                log.error(SCENARIO_FAILURE_HOOKS_PROCESSING_FAILED);
                            }
                            break;
                        }
                    }

                    if (result.isSuccessful()) {
                        long runStepIndex = 0;
                        for (PreparedStep step : testScenario.getExecutionSteps()) {
                            if (kartaRuntime.shouldStepNeedNotBeRun(runInfo, step)) {
                                continue;
                            }

                            eventProcessor.raiseEvent(new ScenarioStepStartEvent(runName, featureName, iterationIndex, testScenario.getName(), step));
                            StepResult stepResult = kartaRuntime.runStep(runInfo, step);
                            stepResult.setStepIndex(runStepIndex++);
                            eventProcessor.raiseEvent(
                                    new ScenarioStepCompleteEvent(runName, featureName, iterationIndex, testScenario.getName(), step, stepResult));
                            result.getRunResults().add(new SerializableKVP<>(step.getIdentifier(), stepResult));
                            result.getIncidents().addAll(stepResult.getIncidents());

                            if (!stepResult.isPassed()) {
                                result.setSuccessful(false);
                                if ((tags != null) && !eventProcessor.scenarioFailed(runName, runName, testScenario, tags, result)) {
                                    log.error(SCENARIO_FAILURE_HOOKS_PROCESSING_FAILED);
                                }
                                break;
                            }
                        }
                    }
                }
            } catch (InterruptedException ie) {
                throw ie;
            } catch (Throwable t) {
                log.error("Exception occurred during scenario run", t);
                log.error(ExceptionUtils.getStackTrace(t));
                result.setError(true);
                result.getIncidents().add(TestIncident.builder().thrownCause(t).build());
            } finally {
                try {
                    long teardownStepIndex = 0;
                    for (PreparedStep step : testScenario.getTearDownSteps()) {
                        if (kartaRuntime.shouldStepNeedNotBeRun(runInfo, step)) {
                            continue;
                        }

                        eventProcessor.raiseEvent(
                                new ScenarioTearDownStepStartEvent(runName, featureName, iterationIndex, testScenario.getName(), step));
                        StepResult stepResult = kartaRuntime.runStep(runInfo, step);
                        stepResult.setStepIndex(teardownStepIndex++);
                        eventProcessor.raiseEvent(
                                new ScenarioTearDownStepCompleteEvent(runName, featureName, iterationIndex, testScenario.getName(), step, stepResult));
                        result.getTearDownResults().add(new SerializableKVP<>(step.getIdentifier(), stepResult));
                        result.getIncidents().addAll(stepResult.getIncidents());

                        if (!stepResult.isPassed()) {
                            result.setSuccessful(false);
                            if ((tags != null) && !eventProcessor.scenarioFailed(runName, runName, testScenario, tags, result)) {
                                log.error(SCENARIO_FAILURE_HOOKS_PROCESSING_FAILED);
                            }
                            break;
                        }
                    }

                    if (tags != null) {
                        if (!eventProcessor.scenarioStop(runName, featureName, testScenario, tags)) {
                            result.setError(true);
                        }
                    }
                } catch (Throwable t) {
                    log.error("Exception occurred during scenario run", t);
                    log.error(ExceptionUtils.getStackTrace(t));
                    result.setError(true);
                    result.getIncidents().add(TestIncident.builder().thrownCause(t).build());
                } finally {
                    result.setEndTime(new Date());
                }
            }
        } else {
            try {
                result = minionToUse.runTestScenario(runInfo, featureName, iterationIndex, testScenario, scenarioIterationNumber);
                result.processRemoteResults();
            } catch (RemoteException e) {
                log.error("Exception occured when running scenario " + testScenario + " remotely on minion " + minionToUse, e);
            }
        }

        updateResultCallBack();
        return result;
    }
}
