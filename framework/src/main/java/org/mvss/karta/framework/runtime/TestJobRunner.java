package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.chaos.ChaosActionTreeNode;
import org.mvss.karta.framework.core.SerializableKVP;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.core.TestJobResult;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.event.ChaosActionJobCompleteEvent;
import org.mvss.karta.framework.runtime.event.ChaosActionJobStartEvent;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.JobStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.JobStepStartEvent;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TestJobRunner
{
   public static TestJobResult run( KartaRuntime kartaRuntime, RunInfo runInfo, String featureName, TestJob job, long iterationIndex, BeanRegistry contextBeanRegistry ) throws Throwable
   {
      EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
      String runName = runInfo.getRunName();
      log.debug( "Running job: " + job );
      TestJobResult testJobResult = new TestJobResult();

      testJobResult.setIterationIndex( iterationIndex );

      HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

      switch ( job.getJobType() )
      {
         case CHAOS:
            ChaosActionTreeNode chaosConfiguration = job.getChaosConfiguration();
            if ( chaosConfiguration == null )
            {
               testJobResult.setError( true );
            }
            else
            {
               if ( !chaosConfiguration.checkForValidity() )
               {
                  log.error( "Chaos configuration has errors " + chaosConfiguration );
               }

               ArrayList<ChaosAction> chaosActionsToPerform = chaosConfiguration.nextChaosActions( kartaRuntime.getRandom() );
               // TODO: Handle chaos action being empty
               for ( ChaosAction chaosAction : chaosActionsToPerform )
               {
                  eventProcessor.raiseEvent( new ChaosActionJobStartEvent( runName, featureName, job, iterationIndex, chaosAction ) );
                  StepResult result = kartaRuntime.runChaosAction( runInfo, featureName, iterationIndex, job.getName(), variables, job.getTestDataSet(), chaosAction, contextBeanRegistry );
                  eventProcessor.raiseEvent( new ChaosActionJobCompleteEvent( runName, featureName, job, iterationIndex, chaosAction, result ) );
               }
            }
            break;

         case STEPS:
            ArrayList<TestStep> steps = job.getSteps();

            if ( steps == null )
            {
               testJobResult.setError( true );
            }
            else
            {
               long stepIndex = 0;
               for ( TestStep step : steps )
               {
                  eventProcessor.raiseEvent( new JobStepStartEvent( runName, featureName, job, iterationIndex, step ) );
                  StepResult result = kartaRuntime.runStep( runInfo, featureName, iterationIndex, job.getName(), variables, job.getTestDataSet(), step, contextBeanRegistry );
                  result.setStepIndex( stepIndex++ );
                  eventProcessor.raiseEvent( new JobStepCompleteEvent( runName, featureName, job, iterationIndex, step, result ) );

                  testJobResult.getStepResults().add( new SerializableKVP<String, StepResult>( step.getStep(), result ) );
                  if ( !result.isPassed() )
                  {
                     testJobResult.setSuccessful( true );
                     testJobResult.setEndTime( new Date() );
                     break;
                  }
               }
            }
            break;

         default:
            testJobResult.setError( true );
            break;
      }

      testJobResult.setEndTime( new Date() );
      return testJobResult;
   }
}
