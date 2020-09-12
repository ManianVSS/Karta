package org.mvss.karta.framework.runtime.interfaces;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.TestFailureException;

public interface StepRunner
{
   void initStepRepository( HashMap<String, Serializable> properties ) throws Throwable;

   boolean runStep( TestStep testStep, TestExecutionContext testExecutionContext ) throws TestFailureException;
}
