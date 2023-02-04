package org.mvss.karta.framework.runtime;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.framework.models.chaos.ChaosAction;
import org.mvss.karta.framework.models.chaos.ChaosActionTreeNode;
import org.mvss.karta.framework.models.event.ChaosActionJobCompleteEvent;
import org.mvss.karta.framework.models.event.ChaosActionJobStartEvent;
import org.mvss.karta.framework.models.event.JobStepCompleteEvent;
import org.mvss.karta.framework.models.event.JobStepStartEvent;
import org.mvss.karta.framework.models.generic.SerializableKVP;
import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.models.result.TestJobResult;
import org.mvss.karta.framework.models.run.RunInfo;
import org.mvss.karta.framework.models.test.PreparedStep;
import org.mvss.karta.framework.models.test.TestJob;
import org.mvss.karta.framework.models.test.TestStep;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Log4j2
public class TestJobRunner {
    public static TestJobResult run(KartaRuntime kartaRuntime, RunInfo runInfo, String featureName, TestJob job, int iterationIndex,
                                    BeanRegistry contextBeanRegistry) throws Throwable {
        EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
        String runName = runInfo.getRunName();
        log.debug("Running job: " + job);
        TestJobResult testJobResult = new TestJobResult();

        testJobResult.setIterationIndex(iterationIndex);

        HashMap<String, Serializable> variables = new HashMap<>();

        switch (job.getJobType()) {
            case CHAOS:
                ChaosActionTreeNode chaosConfiguration = job.getChaosConfiguration();
                if (chaosConfiguration == null) {
                    testJobResult.setError(true);
                } else {
                    if (!chaosConfiguration.checkForValidity()) {
                        log.error("Chaos configuration has errors " + chaosConfiguration);
                    }

                    ArrayList<ChaosAction> chaosActionsToPerform = chaosConfiguration.nextChaosActions(kartaRuntime.getRandom());
                    // TODO: Handle chaos action being empty
                    for (ChaosAction chaosAction : chaosActionsToPerform) {
                        eventProcessor.raiseEvent(new ChaosActionJobStartEvent(runName, featureName, job, iterationIndex, chaosAction));
                        StepResult result = kartaRuntime.runChaosAction(runInfo, featureName, iterationIndex, job.getName(), variables,
                                job.getTestDataSet(), chaosAction, contextBeanRegistry);
                        eventProcessor.raiseEvent(new ChaosActionJobCompleteEvent(runName, featureName, job, iterationIndex, chaosAction, result));
                    }
                }
                break;

            case STEPS:
                ArrayList<TestStep> steps = job.getSteps();

                if (steps == null) {
                    testJobResult.setError(true);
                } else {
                    long stepIndex = 0;
                    for (TestStep step : steps) {
                        PreparedStep preparedStep = kartaRuntime.getPreparedStep(runInfo, featureName, iterationIndex, job.getName(), variables,
                                job.getTestDataSet(), step, contextBeanRegistry);
                        if (kartaRuntime.shouldStepNeedNotBeRun(runInfo, preparedStep)) {
                            continue;
                        }

                        eventProcessor.raiseEvent(new JobStepStartEvent(runName, featureName, job, iterationIndex, step));
                        StepResult result = kartaRuntime.runStep(runInfo, preparedStep);
                        result.setStepIndex(stepIndex++);
                        eventProcessor.raiseEvent(new JobStepCompleteEvent(runName, featureName, job, iterationIndex, step, result));

                        testJobResult.getStepResults().add(new SerializableKVP<>(step.getStep(), result));
                        if (!result.isPassed()) {
                            testJobResult.setSuccessful(true);
                            testJobResult.setEndTime(new Date());
                            break;
                        }
                    }
                }
                break;

            default:
                testJobResult.setError(true);
                break;
        }

        testJobResult.setEndTime(new Date());
        return testJobResult;
    }
}
