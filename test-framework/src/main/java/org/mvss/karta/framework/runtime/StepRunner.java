package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.core.TestStep;

public interface StepRunner
{
   void initStepRepository( HashMap<String, Serializable> properties ) throws Throwable;

   boolean runStep( TestStep testStep, TestExecutionContext testExecutionContext ) throws TestFailureException;
}
