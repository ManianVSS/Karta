package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.chaos.ChaosActionTreeNode;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.minions.KartaMinionRegistry;
import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.ScenarioChaosActionCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioChaosActionStartEvent;
import org.mvss.karta.framework.runtime.event.ScenarioSetupStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioSetupStepStartEvent;
import org.mvss.karta.framework.runtime.event.ScenarioStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioStepStartEvent;
import org.mvss.karta.framework.runtime.event.ScenarioTearDownStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioTearDownStepStartEvent;
import org.mvss.karta.framework.runtime.event.TestIncidentOccurenceEvent;
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
public class ScenarioRunner implements Callable<ScenarioResult>
{
   private KartaRuntime                  kartaRuntime;
   private StepRunner                    stepRunner;
   private ArrayList<TestDataSource>     testDataSources;

   private String                        runName;
   private String                        featureName;
   private int                           iterationIndex;

   private ArrayList<TestStep>           scenarioSetupSteps;
   private TestScenario                  testScenario;
   private ArrayList<TestStep>           scenarioTearDownSteps;

   private int                           scenarioIterationNumber;

   private ScenarioResult                result;

   @Builder.Default
   private HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

   @Override
   public ScenarioResult call()
   {
      result = new ScenarioResult();
      result.setIterationIndex( iterationIndex );

      EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
      KartaMinionRegistry nodeRegistry = kartaRuntime.getNodeRegistry();

      log.debug( "Running Scenario: " + testScenario );
      int stepIndex = 0;
      HashMap<String, Serializable> testData = new HashMap<String, Serializable>();

      // String featureName = ( feature != null ) ? feature : Constants.UNNAMED;

      // TODO: Handle null steps for steps
      try
      {
         for ( TestStep step : DataUtils.mergeLists( scenarioSetupSteps, testScenario.getSetupSteps() ) )
         {
            TestExecutionContext testExecutionContext = new TestExecutionContext( runName, featureName, iterationIndex, testScenario.getName(), step.getIdentifier(), testData, variables );

            testData = KartaRuntime.getMergedTestData( runName, step.getTestData(), step.getTestDataSet(), testDataSources, new ExecutionStepPointer( featureName, testScenario.getName(), stepRunner.sanitizeStepDefinition( step.getIdentifier() ),
                                                                                                                                                      scenarioIterationNumber, stepIndex++ ) );
            // log.debug( "Step test data is " + testData.toString() );
            testExecutionContext.setData( testData );

            eventProcessor.raiseEvent( new ScenarioSetupStepStartEvent( runName, featureName, iterationIndex, testScenario.getName(), step ) );

            StepResult stepResult = null;
            if ( StringUtils.isNotEmpty( step.getNode() ) )
            {
               stepResult = nodeRegistry.getNode( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
            }
            else
            {
               stepResult = stepRunner.runStep( step, testExecutionContext );
            }

            for ( TestIncident incident : stepResult.getIncidents() )
            {
               eventProcessor.raiseEvent( new TestIncidentOccurenceEvent( runName, featureName, iterationIndex, testScenario.getName(), step.getIdentifier(), incident ) );
            }

            for ( Event event : stepResult.getEvents() )
            {
               eventProcessor.raiseEvent( event );
            }

            DataUtils.mergeVariables( stepResult.getResults(), variables );

            eventProcessor.raiseEvent( new ScenarioSetupStepCompleteEvent( runName, featureName, iterationIndex, testScenario.getName(), step, stepResult ) );
            result.getSetupResults().put( step.getIdentifier(), stepResult.isPassed() );
            result.getIncidents().addAll( stepResult.getIncidents() );

            if ( !stepResult.isPassed() )
            {
               result.setSuccessful( false );
               break;
            }

         }

         if ( result.isSuccessful() )
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
                  TestExecutionContext testExecutionContext = new TestExecutionContext( runName, featureName, iterationIndex, testScenario.getName(), chaosAction.getName(), testData, variables );

                  log.debug( "Performing chaos action: " + chaosAction );

                  testData = KartaRuntime.getMergedTestData( runName, null, null, testDataSources, new ExecutionStepPointer( featureName, testScenario.getName(), chaosAction.getName(), iterationIndex, 0 ) );
                  // log.debug( "Chaos test data is " + testData.toString() );

                  eventProcessor.raiseEvent( new ScenarioChaosActionStartEvent( runName, featureName, iterationIndex, testScenario.getName(), chaosAction ) );

                  StepResult stepResult = null;
                  if ( StringUtils.isNotEmpty( chaosAction.getNode() ) )
                  {
                     stepResult = nodeRegistry.getNode( chaosAction.getNode() ).performChaosAction( stepRunner.getPluginName(), chaosAction, testExecutionContext );
                  }
                  else
                  {
                     stepResult = stepRunner.performChaosAction( chaosAction, testExecutionContext );
                  }

                  for ( TestIncident incident : stepResult.getIncidents() )
                  {
                     eventProcessor.raiseEvent( new TestIncidentOccurenceEvent( runName, featureName, iterationIndex, testScenario.getName(), chaosAction.getName(), incident ) );
                  }

                  for ( Event event : stepResult.getEvents() )
                  {
                     eventProcessor.raiseEvent( event );
                  }

                  DataUtils.mergeVariables( stepResult.getResults(), variables );

                  eventProcessor.raiseEvent( new ScenarioChaosActionCompleteEvent( runName, featureName, iterationIndex, testScenario.getName(), chaosAction, stepResult ) );
                  result.getChaosActionResults().put( chaosAction.getName(), stepResult.isPassed() );
                  result.getIncidents().addAll( stepResult.getIncidents() );

                  if ( !stepResult.isPassed() )
                  {
                     result.setSuccessful( false );
                     break;
                  }
               }
            }

            if ( result.isSuccessful() )
            {
               for ( TestStep step : testScenario.getExecutionSteps() )
               {
                  TestExecutionContext testExecutionContext = new TestExecutionContext( runName, featureName, iterationIndex, testScenario.getName(), step.getIdentifier(), testData, variables );

                  testData = KartaRuntime.getMergedTestData( runName, step.getTestData(), step.getTestDataSet(), testDataSources, new ExecutionStepPointer( featureName, testScenario.getName(), stepRunner.sanitizeStepDefinition( step.getIdentifier() ),
                                                                                                                                                            scenarioIterationNumber, stepIndex++ ) );
                  // log.debug( "Step test data is " + testData.toString() );
                  testExecutionContext.setData( testData );
                  eventProcessor.raiseEvent( new ScenarioStepStartEvent( runName, featureName, iterationIndex, testScenario.getName(), step ) );

                  StepResult stepResult = null;
                  if ( StringUtils.isNotEmpty( step.getNode() ) )
                  {
                     // TODO: Handle missing node info
                     stepResult = nodeRegistry.getNode( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
                  }
                  else
                  {
                     stepResult = stepRunner.runStep( step, testExecutionContext );
                  }

                  for ( TestIncident incident : stepResult.getIncidents() )
                  {
                     eventProcessor.raiseEvent( new TestIncidentOccurenceEvent( runName, featureName, iterationIndex, testScenario.getName(), step.getIdentifier(), incident ) );
                  }

                  for ( Event event : stepResult.getEvents() )
                  {
                     eventProcessor.raiseEvent( event );
                  }

                  DataUtils.mergeVariables( stepResult.getResults(), variables );

                  eventProcessor.raiseEvent( new ScenarioStepCompleteEvent( runName, featureName, iterationIndex, testScenario.getName(), step, stepResult ) );
                  result.getRunResults().put( step.getIdentifier(), stepResult.isPassed() );
                  result.getIncidents().addAll( stepResult.getIncidents() );

                  if ( !stepResult.isPassed() )
                  {
                     result.setSuccessful( false );
                     break;
                  }
               }
            }
         }
      }
      catch ( Throwable t )
      {
         log.error( "Exception occured during scenario run", t );
         log.error( ExceptionUtils.getStackTrace( t ) );
         result.setError( true );
         result.getIncidents().add( TestIncident.builder().thrownCause( t ).build() );
      }
      finally
      {
         try
         {
            for ( TestStep step : DataUtils.mergeLists( testScenario.getTearDownSteps(), scenarioTearDownSteps ) )
            {
               testData = KartaRuntime.getMergedTestData( runName, step.getTestData(), step.getTestDataSet(), testDataSources, new ExecutionStepPointer( featureName, testScenario.getName(), stepRunner.sanitizeStepDefinition( step.getIdentifier() ),
                                                                                                                                                         scenarioIterationNumber, stepIndex++ ) );

               TestExecutionContext testExecutionContext = new TestExecutionContext( runName, featureName, iterationIndex, testScenario.getName(), step.getIdentifier(), testData, variables );

               // log.debug( "Step test data is " + testData.toString() );
               testExecutionContext.setData( testData );

               eventProcessor.raiseEvent( new ScenarioTearDownStepStartEvent( runName, featureName, iterationIndex, testScenario.getName(), step ) );
               StepResult stepResult = new StepResult();

               if ( StringUtils.isNotEmpty( step.getNode() ) )
               {
                  stepResult = nodeRegistry.getNode( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
               }
               else
               {
                  stepResult = stepRunner.runStep( step, testExecutionContext );
               }

               for ( TestIncident incident : stepResult.getIncidents() )
               {
                  eventProcessor.raiseEvent( new TestIncidentOccurenceEvent( runName, featureName, iterationIndex, testScenario.getName(), step.getIdentifier(), incident ) );
               }

               for ( Event event : stepResult.getEvents() )
               {
                  eventProcessor.raiseEvent( event );
               }

               DataUtils.mergeVariables( stepResult.getResults(), variables );

               eventProcessor.raiseEvent( new ScenarioTearDownStepCompleteEvent( runName, featureName, iterationIndex, testScenario.getName(), step, stepResult ) );
               result.getTearDownResults().put( step.getIdentifier(), stepResult.isPassed() );
               result.getIncidents().addAll( stepResult.getIncidents() );

               if ( !stepResult.isPassed() )
               {
                  result.setSuccessful( false );
               }
            }
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during scenario run", t );
            log.error( ExceptionUtils.getStackTrace( t ) );
            result.setError( true );
            result.getIncidents().add( TestIncident.builder().thrownCause( t ).build() );
         }
         finally
         {
            result.setEndTime( new Date() );
         }
      }
      return result;
   }
}
