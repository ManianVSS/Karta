package org.mvss.karta.framework.runtime.interfaces;

import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.TestFailureException;

public interface StepRunner extends Plugin
{
   StepResult runStep( TestStep testStep, TestExecutionContext testExecutionContext ) throws TestFailureException;
}
