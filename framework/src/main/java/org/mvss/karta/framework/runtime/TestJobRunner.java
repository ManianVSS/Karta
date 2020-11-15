package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.chaos.ChaosActionTreeNode;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.minions.KartaMinionRegistry;
import org.mvss.karta.framework.runtime.event.ChaosActionJobCompleteEvent;
import org.mvss.karta.framework.runtime.event.ChaosActionJobStartEvent;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.JobStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.JobStepStartEvent;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;
import org.mvss.karta.framework.utils.DataUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TestJobRunner
{
   public static boolean run( KartaRuntime kartaRuntime, StepRunner stepRunner, ArrayList<TestDataSource> testDataSources, String runName, TestFeature feature, TestJob job, int iterationIndex ) throws Throwable
   {
      EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
      KartaMinionRegistry nodeRegistry = kartaRuntime.getNodeRegistry();

      log.debug( "Running job: " + job );

      HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
      HashMap<String, Serializable> variables = new HashMap<String, Serializable>();
      // TestExecutionContext testExecutionContext = new TestExecutionContext( runName, testProperties, testData, variables );

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
                  TestExecutionContext testExecutionContext = new TestExecutionContext( runName, feature.getName(), iterationIndex, Constants.JOB + job.getName(), chaosAction.getName(), testData, variables );

                  testData = KartaRuntime.getMergedTestData( runName, null, null, testDataSources, new ExecutionStepPointer( feature.getName(), job.getName(), chaosAction.getName(), iterationIndex, 0 ) );
                  // log.debug( "Step test data is " + testData.toString() );
                  testExecutionContext.setData( testData );

                  log.debug( "Performing chaos action: " + chaosAction );

                  eventProcessor.raiseEvent( new ChaosActionJobStartEvent( runName, job, iterationIndex, chaosAction ) );

                  StepResult result = new StepResult();
                  if ( StringUtils.isNotEmpty( chaosAction.getNode() ) )
                  {
                     result = nodeRegistry.getNode( chaosAction.getNode() ).performChaosAction( stepRunner.getPluginName(), chaosAction, testExecutionContext );
                     DataUtils.mergeVariables( result.getResults(), testExecutionContext.getVariables() );
                  }
                  else
                  {
                     result = stepRunner.performChaosAction( chaosAction, testExecutionContext );
                  }

                  eventProcessor.raiseEvent( new ChaosActionJobCompleteEvent( runName, job, iterationIndex, chaosAction, result ) );
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

            int stepIndex = 0;

            for ( TestStep step : steps )
            {
               testData = KartaRuntime.getMergedTestData( runName, step.getTestData(), step.getTestDataSet(), testDataSources, new ExecutionStepPointer( feature.getName(), job.getName(), stepRunner.sanitizeStepDefinition( step.getIdentifier() ),
                                                                                                                                                         iterationIndex, stepIndex++ ) );
               // log.debug( "Step test data is " + testData.toString() );
               TestExecutionContext testExecutionContext = new TestExecutionContext( runName, feature.getName(), iterationIndex, Constants.JOB + job.getName(), step.getIdentifier(), testData, variables );

               testExecutionContext.setData( testData );
               eventProcessor.raiseEvent( new JobStepStartEvent( runName, feature, job, iterationIndex, step ) );
               StepResult result = new StepResult();

               if ( StringUtils.isNotEmpty( step.getNode() ) )
               {
                  result = nodeRegistry.getNode( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
                  DataUtils.mergeVariables( result.getResults(), testExecutionContext.getVariables() );
               }
               else
               {
                  result = stepRunner.runStep( step, testExecutionContext );
               }

               eventProcessor.raiseEvent( new JobStepCompleteEvent( runName, feature, job, iterationIndex, step, result ) );

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
