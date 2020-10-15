package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class ScenarioTearDownStepStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private TestFeature       feature;
   private long              iterationNumber;
   private TestScenario      scenario;
   private TestStep          scenarioTearDownStep;

   public ScenarioTearDownStepStartEvent( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioTearDownStep )
   {
      super( StandardEventsTypes.SCENARIO_TEARDOWN_STEP_START_EVENT, runName );
      this.feature = feature;
      this.iterationNumber = iterationNumber;
      this.scenario = scenario;
      this.scenarioTearDownStep = scenarioTearDownStep;
   }

   @Builder
   public ScenarioTearDownStepStartEvent( String runName, UUID id, Date timeOfOccurrence, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioTearDownStep )
   {
      super( StandardEventsTypes.SCENARIO_TEARDOWN_STEP_START_EVENT, runName, id, timeOfOccurrence );
      this.feature = feature;
      this.iterationNumber = iterationNumber;
      this.scenario = scenario;
      this.scenarioTearDownStep = scenarioTearDownStep;
   }
}