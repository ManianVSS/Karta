package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.chaos.ChaosActionTreeNode;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.minions.KartaMinionRegistry;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.ScenarioChaosActionCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioChaosActionStartEvent;
import org.mvss.karta.framework.runtime.event.ScenarioSetupStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioSetupStepStartEvent;
import org.mvss.karta.framework.runtime.event.ScenarioStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioStepStartEvent;
import org.mvss.karta.framework.runtime.event.ScenarioTearDownStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioTearDownStepStartEvent;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;
import org.mvss.karta.framework.utils.DataUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
@JsonInclude( value = Include.NON_ABSENT, content = Include.NON_ABSENT )
@Builder
public class ScenarioRunner implements Runnable
{
   private KartaRuntime                  kartaRuntime;
   private StepRunner                    stepRunner;
   private ArrayList<TestDataSource>     testDataSources;

   private String                        runName;
   private TestFeature                   feature;
   private int                           iterationIndex;

   private TestScenario                  testScenario;
   private int                           scenarioIterationNumber;

   private ScenarioResult                result;

   @Builder.Default
   private HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

   // @SuppressWarnings( "resource" )
   @Override
   public void run()
   {
      result = new ScenarioResult();
      result.setIterationIndex( iterationIndex );

      EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
      KartaMinionRegistry nodeRegistry = kartaRuntime.getNodeRegistry();

      log.debug( "Running Scenario: " + testScenario );
      int stepIndex = 0;
      HashMap<String, Serializable> testData = new HashMap<String, Serializable>();

      String featureName = ( feature != null ) ? feature.getName() : Constants.UNNAMED;

      // TODO: Handle null steps for steps
      try
      {
         ArrayList<TestStep> mergedSetupSteps = new ArrayList<TestStep>();
         if ( ( feature != null ) && ( feature.getScenarioSetupSteps() != null ) )
         {
            mergedSetupSteps.addAll( feature.getScenarioSetupSteps() );
         }
         if ( testScenario.getSetupSteps() != null )
         {
            mergedSetupSteps.addAll( testScenario.getSetupSteps() );
         }

         for ( TestStep step : mergedSetupSteps )
         {
            TestExecutionContext testExecutionContext = new TestExecutionContext( runName, feature.getName(), iterationIndex, testScenario.getName(), step.getIdentifier(), testData, variables );

            testData = KartaRuntime
                     .getMergedTestData( runName, step.getTestData(), testDataSources, new ExecutionStepPointer( featureName, testScenario.getName(), stepRunner.sanitizeStepDefinition( step.getIdentifier() ), scenarioIterationNumber, stepIndex++ ) );
            // log.debug( "Step test data is " + testData.toString() );
            testExecutionContext.setData( testData );

            eventProcessor.raiseEvent( new ScenarioSetupStepStartEvent( runName, feature, iterationIndex, testScenario, step ) );

            StepResult stepResult = null;
            if ( StringUtils.isNotEmpty( step.getNode() ) )
            {
               stepResult = nodeRegistry.getNode( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
               DataUtils.mergeVariables( testExecutionContext.getVariables(), variables );
               DataUtils.mergeVariables( stepResult.getResults(), variables );
            }
            else
            {
               stepResult = stepRunner.runStep( step, testExecutionContext );
            }

            eventProcessor.raiseEvent( new ScenarioSetupStepCompleteEvent( runName, feature, iterationIndex, testScenario, step, stepResult ) );
            result.getSetupResults().put( step, stepResult );

            if ( !stepResult.isSuccesssful() )
            {
               result.setSuccesssful( false );
               break;
            }

         }

         if ( result.isSuccesssful() )
         {
            ChaosActionTreeNode chaosConfiguration = testScenario.getChaosConfiguration();
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
                  TestExecutionContext testExecutionContext = new TestExecutionContext( runName, feature.getName(), iterationIndex, testScenario.getName(), chaosAction.getName(), testData, variables );

                  log.debug( "Performing chaos action: " + chaosAction );

                  testData = KartaRuntime.getMergedTestData( runName, null, testDataSources, new ExecutionStepPointer( feature.getName(), testScenario.getName(), chaosAction.getName(), iterationIndex, 0 ) );
                  // log.debug( "Chaos test data is " + testData.toString() );

                  eventProcessor.raiseEvent( new ScenarioChaosActionStartEvent( runName, feature, iterationIndex, testScenario, chaosAction ) );

                  StepResult stepResult = null;
                  if ( StringUtils.isNotEmpty( chaosAction.getNode() ) )
                  {
                     stepResult = nodeRegistry.getNode( chaosAction.getNode() ).performChaosAction( stepRunner.getPluginName(), chaosAction, testExecutionContext );
                     DataUtils.mergeVariables( stepResult.getResults(), testExecutionContext.getVariables() );
                  }
                  else
                  {
                     stepResult = stepRunner.performChaosAction( chaosAction, testExecutionContext );
                  }

                  eventProcessor.raiseEvent( new ScenarioChaosActionCompleteEvent( runName, feature, iterationIndex, testScenario, chaosAction, stepResult ) );
                  result.getChaosActionResults().put( chaosAction, stepResult );

                  if ( !stepResult.isSuccesssful() )
                  {
                     result.setSuccesssful( false );
                     break;
                  }
               }
            }

            if ( result.isSuccesssful() )
            {
               for ( TestStep step : testScenario.getExecutionSteps() )
               {
                  TestExecutionContext testExecutionContext = new TestExecutionContext( runName, feature.getName(), iterationIndex, testScenario.getName(), step.getIdentifier(), testData, variables );

                  testData = KartaRuntime
                           .getMergedTestData( runName, step.getTestData(), testDataSources, new ExecutionStepPointer( featureName, testScenario.getName(), stepRunner.sanitizeStepDefinition( step.getIdentifier() ), scenarioIterationNumber, stepIndex++ ) );
                  // log.debug( "Step test data is " + testData.toString() );
                  testExecutionContext.setData( testData );
                  eventProcessor.raiseEvent( new ScenarioStepStartEvent( runName, feature, iterationIndex, testScenario, step ) );

                  StepResult stepResult = null;
                  if ( StringUtils.isNotEmpty( step.getNode() ) )
                  {
                     // TODO: Handle missing node info
                     stepResult = nodeRegistry.getNode( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
                     DataUtils.mergeVariables( stepResult.getResults(), testExecutionContext.getVariables() );
                  }
                  else
                  {
                     stepResult = stepRunner.runStep( step, testExecutionContext );
                  }

                  eventProcessor.raiseEvent( new ScenarioStepCompleteEvent( runName, feature, iterationIndex, testScenario, step, stepResult ) );
                  result.getRunResults().put( step, stepResult );

                  if ( !stepResult.isSuccesssful() )
                  {
                     result.setSuccesssful( false );
                     break;
                  }
               }
            }
         }
      }
      catch ( Throwable t )
      {
         log.error( t );
         log.error( ExceptionUtils.getStackTrace( t ) );
         result.setError( true );
         result.getIncidents().add( TestIncident.builder().thrownCause( t ).build() );
      }
      finally
      {
         try
         {
            ArrayList<TestStep> mergedTearDownSteps = new ArrayList<TestStep>();

            if ( testScenario.getTearDownSteps() != null )
            {
               mergedTearDownSteps.addAll( testScenario.getTearDownSteps() );
            }
            if ( ( feature != null ) && ( feature.getScenarioTearDownSteps() != null ) )
            {
               mergedTearDownSteps.addAll( feature.getScenarioTearDownSteps() );
            }

            for ( TestStep step : mergedTearDownSteps )
            {
               testData = KartaRuntime
                        .getMergedTestData( runName, step.getTestData(), testDataSources, new ExecutionStepPointer( featureName, testScenario.getName(), stepRunner.sanitizeStepDefinition( step.getIdentifier() ), scenarioIterationNumber, stepIndex++ ) );

               TestExecutionContext testExecutionContext = new TestExecutionContext( runName, feature.getName(), iterationIndex, testScenario.getName(), step.getIdentifier(), testData, variables );

               // log.debug( "Step test data is " + testData.toString() );
               testExecutionContext.setData( testData );

               eventProcessor.raiseEvent( new ScenarioTearDownStepStartEvent( runName, feature, iterationIndex, testScenario, step ) );
               StepResult stepResult = new StepResult();

               if ( StringUtils.isNotEmpty( step.getNode() ) )
               {
                  stepResult = nodeRegistry.getNode( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
                  DataUtils.mergeVariables( stepResult.getResults(), testExecutionContext.getVariables() );
               }
               else
               {
                  stepResult = stepRunner.runStep( step, testExecutionContext );
               }

               eventProcessor.raiseEvent( new ScenarioTearDownStepCompleteEvent( runName, feature, iterationIndex, testScenario, step, stepResult ) );
               result.getTearDownResults().put( step, stepResult );

               if ( !stepResult.isSuccesssful() )
               {
                  result.setSuccesssful( false );
               }
            }
         }
         catch ( Throwable t )
         {
            log.error( t );
            log.error( ExceptionUtils.getStackTrace( t ) );
            result.setError( true );
            result.getIncidents().add( TestIncident.builder().thrownCause( t ).build() );
         }
         finally
         {
            result.setEndTime( new Date() );
         }
      }
   }
}
