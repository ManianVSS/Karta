package org.mvss.karta.framework.plugins;

import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.models.run.TestExecutionContext;
import org.mvss.karta.framework.models.test.PreparedChaosAction;
import org.mvss.karta.framework.models.test.PreparedStep;
import org.mvss.karta.framework.runtime.TestFailureException;

public interface StepRunner extends Plugin {
    boolean conditionImplemented(String conditionIdentifier);

    boolean runCondition(TestExecutionContext testExecutionContext, String conditionIdentifier);

    boolean stepImplemented(String identifier);

    StepResult runStep(PreparedStep testStep) throws TestFailureException;

    String sanitizeStepIdentifier(String stepDefinition);


    boolean chaosActionImplemented(String name);

    StepResult performChaosAction(PreparedChaosAction chaosAction) throws TestFailureException;
}
