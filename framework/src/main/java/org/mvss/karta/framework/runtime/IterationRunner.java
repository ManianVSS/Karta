package org.mvss.karta.framework.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.ScenarioCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioStartEvent;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;

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
public class IterationRunner implements Runnable
{
   private KartaRuntime                         kartaRuntime;
   private StepRunner                           stepRunner;
   private ArrayList<TestDataSource>            testDataSources;

   private TestFeature                          feature;
   private String                               runName;

   private int                                  iterationIndex;

   private ArrayList<TestScenario>              scenariosToRun;

   private HashMap<TestScenario, AtomicInteger> scenarioIterationIndexMap;

   @Override
   public void run()
   {
      EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
      log.debug( "Iteration " + iterationIndex + " with scenarios " + scenariosToRun );

      for ( TestScenario testScenario : scenariosToRun )
      {
         int scenarioIterationNumber = ( ( scenarioIterationIndexMap != null ) && ( scenarioIterationIndexMap.containsKey( testScenario ) ) ) ? scenarioIterationIndexMap.get( testScenario ).getAndIncrement() : 0;
         log.debug( "Running Scenario: " + testScenario.getName() + "[" + scenarioIterationNumber + "]:" );

         eventProcessor.raiseEvent( new ScenarioStartEvent( runName, feature, iterationIndex, testScenario ) );
         ScenarioRunner.builder().kartaRuntime( kartaRuntime ).stepRunner( stepRunner ).testDataSources( testDataSources ).feature( feature ).runName( runName ).iterationIndex( iterationIndex ).testScenario( testScenario )
                  .scenarioIterationNumber( scenarioIterationNumber ).build().run();

         eventProcessor.raiseEvent( new ScenarioCompleteEvent( runName, feature, iterationIndex, testScenario ) );
      }
      // TODO: Scenario passed handler;
   }
}
