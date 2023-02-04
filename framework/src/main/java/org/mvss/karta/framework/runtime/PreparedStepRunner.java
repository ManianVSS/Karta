package org.mvss.karta.framework.runtime;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.models.run.RunInfo;
import org.mvss.karta.framework.models.test.PreparedStep;
import org.mvss.karta.framework.plugins.StepRunner;
import org.mvss.karta.framework.utils.ParallelCausesException;
import org.mvss.karta.framework.utils.ThreadUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
@Builder
public class PreparedStepRunner implements Callable<StepResult> {
    private KartaRuntime kartaRuntime;
    private RunInfo runInfo;
    private String nodeName;
    private PreparedStep step;
    private Consumer<StepResult> resultConsumer;

    public StepResult execute() throws InterruptedException {
        StepResult stepResult;

        try {
            ArrayList<PreparedStep> nestedSteps = step.getSteps();

            if ((nestedSteps == null) || nestedSteps.isEmpty()) {
                // TODO:Handle null exceptions
                StepRunner stepRunner = kartaRuntime.getStepRunner(runInfo);
                stepResult = stepRunner.runStep(step);
            } else {
                // TODO: Forward test data and test data set from parent step to nested steps
                boolean runInParallel = (step.getRunStepsInParallel() != null) && step.getRunStepsInParallel();

                StepResult cumulativeStepResult = new StepResult();
                List<Callable<StepResult>> preparedStepRunners = nestedSteps.stream()
                        .map(nestedStep -> PreparedStepRunner.builder().kartaRuntime(kartaRuntime).runInfo(runInfo).step(nestedStep).build())
                        .collect(Collectors.toList());
                if (!ThreadUtils.runCallables(preparedStepRunners, cumulativeStepResult::mergeResults, runInParallel, nestedSteps.size())) {
                    if (runInParallel) {
                        log.error("Failed awaiting termination of step executor service");
                    }
                }
                stepResult = cumulativeStepResult;
            }
        } catch (TestFailureException t) {
            stepResult = StandardStepResults.failure(t);
        } catch (ParallelCausesException parallelCausesException) {
            boolean allAreTestFailures = true;
            for (Throwable throwable : parallelCausesException.causeList) {
                if (!(throwable instanceof TestFailureException)) {
                    allAreTestFailures = false;
                    break;
                }
            }
            stepResult = allAreTestFailures ?
                    StandardStepResults.failure(parallelCausesException) :
                    StandardStepResults.error(parallelCausesException);
        } catch (Throwable t) {
            stepResult = StandardStepResults.error(t);
        }

        return stepResult;
    }

    @Override
    public StepResult call() throws InterruptedException {
        Date startTime = new Date();
        StepResult stepResult;//= new StepResult();

        try {
            stepResult = execute();

            if (stepResult.isFailed()) {
                for (int currentRetry = 0; currentRetry < step.getMaxRetries(); currentRetry++) {
                    StepResult retryResult = execute();
                    stepResult.mergeResults(retryResult);

                    if (!retryResult.isFailed()) {
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
            throw e;
        } catch (Throwable t) {
            stepResult = StandardStepResults.error(t);
        }

        stepResult.setStartTime(startTime);

        if (resultConsumer != null) {
            resultConsumer.accept(stepResult);
        }

        return stepResult;
    }
}
