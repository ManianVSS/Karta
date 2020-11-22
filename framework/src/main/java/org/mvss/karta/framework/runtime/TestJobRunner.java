package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.chaos.ChaosActionTreeNode;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.event.ChaosActionJobCompleteEvent;
import org.mvss.karta.framework.runtime.event.ChaosActionJobStartEvent;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.JobStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.JobStepStartEvent;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TestJobRunner
{
   public static boolean run( KartaRuntime kartaRuntime, StepRunner stepRunner, ArrayList<TestDataSource> testDataSources, String runName, String featureName, TestJob job, long iterationIndex ) throws Throwable
   {
      EventProcessor eventProcessor = kartaRuntime.getEventProcessor();

      log.debug( "Running job: " + job );

      HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

      switch ( job.getJobType() )
      {
         case CHAOS:
            ChaosActionTreeNode chaosConfiguration = job.getChaosConfiguration();
            if ( chaosConfiguration != null )
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
                  StepResult result = kartaRuntime.runChaosAction( stepRunner, testDataSources, runName, featureName, iterationIndex, job.getName(), variables, chaosAction );
                  eventProcessor.raiseEvent( new ChaosActionJobCompleteEvent( runName, featureName, job, iterationIndex, chaosAction, result ) );
               }
            }
            else
            {
               return false;
            }
            break;

         case STEPS:
            ArrayList<TestStep> steps = job.getSteps();

            if ( steps == null )
            {
               return false;
            }

            for ( TestStep step : steps )
            {
               eventProcessor.raiseEvent( new JobStepStartEvent( runName, featureName, job, iterationIndex, step ) );
               StepResult result = kartaRuntime.runStep( stepRunner, testDataSources, runName, featureName, iterationIndex, job.getName(), variables, step );
               eventProcessor.raiseEvent( new JobStepCompleteEvent( runName, featureName, job, iterationIndex, step, result ) );

               if ( !result.isPassed() )
               {
                  return false;
               }
            }
            break;

         default:
            return false;
      }

      return true;
   }
}
