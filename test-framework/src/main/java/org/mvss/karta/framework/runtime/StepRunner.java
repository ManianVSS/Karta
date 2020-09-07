package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.core.TestStep;

public interface StepRunner
{
   void initStepRepository( HashMap<String, Serializable> testProperties ) throws Throwable;

   boolean runStep( TestStep testStep, TestExecutionContext testExecutionContext ) throws TestFailureException;

   // boolean runFeature( TestFeature testFeature, HashMap<String, Serializable> testProperties ) throws TestFailureException;
   //
   // boolean runScenario( TestScenario testStep, TestExecutionContext testExecutionContext ) throws TestFailureException;

}
