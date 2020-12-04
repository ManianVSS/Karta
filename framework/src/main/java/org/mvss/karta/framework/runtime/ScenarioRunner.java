package org.mvss.karta.framework.runtime;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mvss.karta.framework.core.PreparedChaosAction;
import org.mvss.karta.framework.core.PreparedScenario;
import org.mvss.karta.framework.core.PreparedStep;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.ScenarioChaosActionCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioChaosActionStartEvent;
import org.mvss.karta.framework.runtime.event.ScenarioSetupStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioSetupStepStartEvent;
import org.mvss.karta.framework.runtime.event.ScenarioStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioStepStartEvent;
import org.mvss.karta.framework.runtime.event.ScenarioTearDownStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioTearDownStepStartEvent;

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
   private KartaRuntime     kartaRuntime;
   private RunInfo          runInfo;

   private String           featureName;
   private long             iterationIndex;

   private PreparedScenario testScenario;

   private long             scenarioIterationNumber;

   private ScenarioResult   result;

   @Override
   public ScenarioResult call()
   {
      String runName = runInfo.getRunName();

      result = new ScenarioResult();
      result.setIterationIndex( iterationIndex );

      EventProcessor eventProcessor = kartaRuntime.getEventProcessor();

      // This should run at scenario runner since this need to run on the node where scenario is to be run
      testScenario.propogateContextBeanRegistry();

      HashSet<String> tags = runInfo.getTags();
      if ( tags != null )
      {
         eventProcessor.scenarioStart( runName, featureName, testScenario, tags );
      }

      log.debug( "Running Scenario: " + testScenario );

      // TODO: Handle null steps for steps
      try
      {
         for ( PreparedStep step : testScenario.getSetupSteps() )
         {
            eventProcessor.raiseEvent( new ScenarioSetupStepStartEvent( runName, featureName, iterationIndex, testScenario.getName(), step ) );

            StepResult stepResult = kartaRuntime.runStep( runInfo, step );

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
            for ( PreparedChaosAction preparedChaosAction : testScenario.getChaosActions() )
            {
               eventProcessor.raiseEvent( new ScenarioChaosActionStartEvent( runName, featureName, iterationIndex, testScenario.getName(), preparedChaosAction ) );

               StepResult stepResult = kartaRuntime.runChaosAction( runInfo, preparedChaosAction );

               eventProcessor.raiseEvent( new ScenarioChaosActionCompleteEvent( runName, featureName, iterationIndex, testScenario.getName(), preparedChaosAction, stepResult ) );
               result.getChaosActionResults().put( preparedChaosAction.getChaosAction().getName(), stepResult.isPassed() );
               result.getIncidents().addAll( stepResult.getIncidents() );

               if ( !stepResult.isPassed() )
               {
                  result.setSuccessful( false );
                  break;
               }
            }

            if ( result.isSuccessful() )
            {
               for ( PreparedStep step : testScenario.getExecutionSteps() )
               {
                  eventProcessor.raiseEvent( new ScenarioStepStartEvent( runName, featureName, iterationIndex, testScenario.getName(), step ) );

                  StepResult stepResult = kartaRuntime.runStep( runInfo, step );

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
            for ( PreparedStep step : testScenario.getTearDownSteps() )
            {
               eventProcessor.raiseEvent( new ScenarioTearDownStepStartEvent( runName, featureName, iterationIndex, testScenario.getName(), step ) );

               StepResult stepResult = kartaRuntime.runStep( runInfo, step );

               eventProcessor.raiseEvent( new ScenarioTearDownStepCompleteEvent( runName, featureName, iterationIndex, testScenario.getName(), step, stepResult ) );
               result.getTearDownResults().put( step.getIdentifier(), stepResult.isPassed() );
               result.getIncidents().addAll( stepResult.getIncidents() );

               if ( !stepResult.isPassed() )
               {
                  result.setSuccessful( false );
               }
            }

            if ( tags != null )
            {
               eventProcessor.scenarioStop( runName, featureName, testScenario, tags );
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
