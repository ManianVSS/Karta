package org.mvss.karta.samples.runner;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.StepRunner;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.TestFailureException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class KeywordStepRunner implements StepRunner
{
   private HashMap<String, RunnableWithThrow> stepMap = new HashMap<String, RunnableWithThrow>();

   @Override
   public void initStepRepository( HashMap<String, Serializable> testProperties ) throws Throwable
   {
      stepMap.put( "step 1", () -> step1() );
      stepMap.put( "step 2", () -> step2() );
      stepMap.put( "step 2", () -> step3() );
   }

   @Override
   public boolean runStep( TestStep testStep, TestExecutionContext testExecutionContext ) throws TestFailureException
   {
      String stepRef = testStep.getStepDefReference();

      if ( !stepMap.containsKey( stepRef ) )
      {
         return false;
      }

      try
      {
         stepMap.get( stepRef ).run();
      }
      catch ( Throwable t )
      {
         return false;
      }

      return true;
   }

   void step1() throws Throwable
   {
      log.info( "Running step1" );
   }

   void step2() throws Throwable
   {
      log.info( "Running step2" );
   }

   void step3() throws Throwable
   {
      log.info( "Running step3" );
   }

}
