package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.mvss.karta.framework.core.PreparedScenario;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.minions.KartaMinion;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.ScenarioCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioStartEvent;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
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
public class IterationRunner implements Callable<HashMap<String, ScenarioResult>>
{
   private KartaRuntime                              kartaRuntime;
   private StepRunner                                stepRunner;
   private ArrayList<TestDataSource>                 testDataSources;

   private String                                    runName;
   private String                                    featureName;

   private long                                      iterationIndex;

   private ArrayList<TestStep>                       scenarioSetupSteps;
   private ArrayList<TestScenario>                   scenariosToRun;
   private ArrayList<TestStep>                       scenarioTearDownSteps;

   @Builder.Default
   private KartaMinion                               minionToUse = null;

   private HashMap<TestScenario, AtomicLong>         scenarioIterationIndexMap;

   @Builder.Default
   private HashMap<String, Serializable>             variables   = new HashMap<String, Serializable>();;

   private HashMap<String, ScenarioResult>           result;

   private Consumer<HashMap<String, ScenarioResult>> resultConsumer;
   private HashSet<String>                           tags;

   @Override
   public HashMap<String, ScenarioResult> call()
   {
      result = new HashMap<String, ScenarioResult>();

      EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
      log.debug( "Iteration " + iterationIndex + " with scenarios " + scenariosToRun );

      for ( TestScenario testScenario : scenariosToRun )
      {
         long scenarioIterationNumber = ( ( scenarioIterationIndexMap != null ) && ( scenarioIterationIndexMap.containsKey( testScenario ) ) ) ? scenarioIterationIndexMap.get( testScenario ).getAndIncrement() : 0;
         log.debug( "Running Scenario: " + testScenario.getName() + "[" + scenarioIterationNumber + "]:" );

         PreparedScenario preparedScenario = null;

         try
         {
            preparedScenario = kartaRuntime.getPreparedScenario( stepRunner, testDataSources, runName, featureName, scenarioIterationNumber, DataUtils.cloneMap( variables ), scenarioSetupSteps, testScenario, scenarioTearDownSteps );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured when preparing scenario " + testScenario + " for running", t );
            continue;
         }

         if ( tags != null )
         {
            eventProcessor.scenarioStart( runName, featureName, testScenario, tags );
         }
         eventProcessor.raiseEvent( new ScenarioStartEvent( runName, featureName, iterationIndex, testScenario ) );

         ScenarioResult scenarioResult = null;

         if ( minionToUse == null )
         {
            ScenarioRunner scenarioRunner = ScenarioRunner.builder().kartaRuntime( kartaRuntime ).stepRunner( stepRunner ).featureName( featureName ).runName( runName ).iterationIndex( iterationIndex ).testScenario( preparedScenario )
                     .scenarioIterationNumber( scenarioIterationNumber ).build();
            scenarioResult = scenarioRunner.call();
         }
         else
         {
            try
            {
               scenarioResult = minionToUse.runTestScenario( stepRunner.getPluginName(), runName, featureName, scenarioIterationNumber, preparedScenario, scenarioIterationNumber );
            }
            catch ( RemoteException e )
            {
               log.error( "Exception occured when running scenario " + testScenario + " remotely on minion " + minionToUse, e );
            }
         }

         if ( scenarioResult != null )
         {
            result.put( testScenario.getName(), scenarioResult );
         }

         eventProcessor.raiseEvent( new ScenarioCompleteEvent( runName, featureName, iterationIndex, testScenario, scenarioResult ) );

         if ( tags != null )
         {
            eventProcessor.scenarioStop( runName, featureName, testScenario, tags );
         }
      }

      if ( resultConsumer != null )
      {
         resultConsumer.accept( result );
      }

      return result;
   }
}
