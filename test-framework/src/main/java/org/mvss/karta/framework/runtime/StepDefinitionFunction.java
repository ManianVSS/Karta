package org.mvss.karta.framework.runtime;

import org.mvss.karta.framework.core.TestStep;

@FunctionalInterface
public interface StepDefinitionFunction
{
   boolean runStep( TestStep testStep, TestExecutionContext testExecutionContext ) throws TestFailureException;
}
