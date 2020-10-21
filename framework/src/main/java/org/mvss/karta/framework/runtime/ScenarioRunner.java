package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.chaos.ChaosActionTreeNode;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
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
public class ScenarioRunner
{
   private KartaRuntime              kartaRuntime;
   private StepRunner                stepRunner;
   private ArrayList<TestDataSource> testDataSources;

   private String                    runName;
   private TestFeature               feature;
   private int                       iterationIndex;

   private TestScenario              testScenario;
   private int                       scenarioIterationNumber;

   @SuppressWarnings( "resource" )
   public boolean run()
   {
      EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
      HashMap<String, HashMap<String, Serializable>> testProperties = kartaRuntime.getConfigurator().getPropertiesStore();
      KartaMinionRegistry minionRegistry = kartaRuntime.getMinionRegistry();

      log.debug( "Running Scenario: " + testScenario );
      int stepIndex = 0;
      HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
      HashMap<String, Serializable> variables = new HashMap<String, Serializable>();
      TestExecutionContext testExecutionContext = new TestExecutionContext( testProperties, testData, variables );

      String featureName = ( feature != null ) ? feature.getName() : Constants.UNNAMED;

      // TODO: Handle null steps for steps
      try
      {
         ArrayList<TestStep> mergedSetupSteps = new ArrayList<TestStep>();
         if ( ( feature != null ) && ( feature.getScenarioSetupSteps() != null ) )
         {
            mergedSetupSteps.addAll( feature.getScenarioSetupSteps() );
         }
         mergedSetupSteps.addAll( testScenario.getScenarioSetupSteps() );

         for ( TestStep step : mergedSetupSteps )
         {
            testData = KartaRuntime.getMergedTestData( step.getTestData(), testDataSources, new ExecutionStepPointer( featureName, testScenario.getName(), stepRunner.sanitizeStepDefinition( step.getIdentifier() ), scenarioIterationNumber, stepIndex++ ) );
            // log.debug( "Step test data is " + testData.toString() );
            testExecutionContext.setData( testData );

            eventProcessor.raiseEvent( new ScenarioSetupStepStartEvent( runName, feature, iterationIndex, testScenario, step ) );

            StepResult result = new StepResult();

            if ( StringUtils.isNotEmpty( step.getNode() ) )
            {
               result = minionRegistry.getMinion( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
               DataUtils.mergeVariables( result.getResults(), testExecutionContext.getVariables() );
            }
            else
            {
               result = stepRunner.runStep( step, testExecutionContext );
            }

            eventProcessor.raiseEvent( new ScenarioSetupStepCompleteEvent( runName, feature, iterationIndex, testScenario, step, result ) );

            if ( !result.isSuccesssful() )
            {
               return false;
            }

         }

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
               log.debug( "Performing chaos action: " + chaosAction );

               eventProcessor.raiseEvent( new ScenarioChaosActionStartEvent( runName, feature, iterationIndex, testScenario, chaosAction ) );

               StepResult result = new StepResult();
               if ( StringUtils.isNotEmpty( chaosAction.getNode() ) )
               {
                  result = minionRegistry.getMinion( chaosAction.getNode() ).performChaosAction( stepRunner.getPluginName(), chaosAction, testExecutionContext );
                  DataUtils.mergeVariables( result.getResults(), testExecutionContext.getVariables() );
               }
               else
               {
                  result = stepRunner.performChaosAction( chaosAction, testExecutionContext );
               }

               eventProcessor.raiseEvent( new ScenarioChaosActionCompleteEvent( runName, feature, iterationIndex, testScenario, chaosAction, result ) );
            }
         }

         for ( TestStep step : testScenario.getScenarioExecutionSteps() )
         {
            testData = KartaRuntime.getMergedTestData( step.getTestData(), testDataSources, new ExecutionStepPointer( featureName, testScenario.getName(), stepRunner.sanitizeStepDefinition( step.getIdentifier() ), scenarioIterationNumber, stepIndex++ ) );
            // log.debug( "Step test data is " + testData.toString() );
            testExecutionContext.setData( testData );
            eventProcessor.raiseEvent( new ScenarioStepStartEvent( runName, feature, iterationIndex, testScenario, step ) );
            StepResult result = new StepResult();

            if ( StringUtils.isNotEmpty( step.getNode() ) )
            {
               // TODO: Handle missing node info
               result = minionRegistry.getMinion( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
               DataUtils.mergeVariables( result.getResults(), testExecutionContext.getVariables() );
            }
            else
            {
               result = stepRunner.runStep( step, testExecutionContext );
            }

            eventProcessor.raiseEvent( new ScenarioStepCompleteEvent( runName, feature, iterationIndex, testScenario, step, result ) );

            if ( !result.isSuccesssful() )
            {
               return false;
            }
         }
      }
      catch ( Throwable t )
      {
         log.error( t );
      }
      finally
      {
         try
         {
            ArrayList<TestStep> mergedTearDownSteps = new ArrayList<TestStep>();
            if ( ( feature != null ) && ( feature.getScenarioTearDownSteps() != null ) )
            {
               mergedTearDownSteps.addAll( feature.getScenarioTearDownSteps() );
            }
            mergedTearDownSteps.addAll( testScenario.getScenarioTearDownSteps() );

            for ( TestStep step : mergedTearDownSteps )
            {
               testData = KartaRuntime
                        .getMergedTestData( step.getTestData(), testDataSources, new ExecutionStepPointer( featureName, testScenario.getName(), stepRunner.sanitizeStepDefinition( step.getIdentifier() ), scenarioIterationNumber, stepIndex++ ) );

               // log.debug( "Step test data is " + testData.toString() );
               testExecutionContext.setData( testData );

               eventProcessor.raiseEvent( new ScenarioTearDownStepStartEvent( runName, feature, iterationIndex, testScenario, step ) );
               StepResult result = new StepResult();

               if ( StringUtils.isNotEmpty( step.getNode() ) )
               {
                  result = minionRegistry.getMinion( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
                  DataUtils.mergeVariables( result.getResults(), testExecutionContext.getVariables() );
               }
               else
               {
                  result = stepRunner.runStep( step, testExecutionContext );
               }

               eventProcessor.raiseEvent( new ScenarioTearDownStepCompleteEvent( runName, feature, iterationIndex, testScenario, step, result ) );

               if ( !result.isSuccesssful() )
               {
                  return false;
               }
            }
         }
         catch ( Throwable t )
         {
            log.error( t );
         }
      }
      return true;
   }
}
