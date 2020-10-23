package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.core.StepResult;
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
public class ScenarioSetupStepCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private TestFeature       feature;
   private int               iterationNumber;
   private TestScenario      scenario;
   private TestStep          scenarioSetupStep;
   private StepResult        result;

   public ScenarioSetupStepCompleteEvent( String runName, TestFeature feature, int iterationNumber, TestScenario scenario, TestStep scenarioSetupStep, StepResult result )
   {
      super( StandardEventsTypes.SCENARIO_SETUP_STEP_COMPLETE_EVENT, runName );
      this.feature = feature;
      this.iterationNumber = iterationNumber;
      this.scenario = scenario;
      this.scenarioSetupStep = scenarioSetupStep;
      this.result = result;
   }

   @Builder
   public ScenarioSetupStepCompleteEvent( String runName, UUID id, Date timeOfOccurrence, TestFeature feature, int iterationNumber, TestScenario scenario, TestStep scenarioSetupStep, StepResult result )
   {
      super( StandardEventsTypes.SCENARIO_SETUP_STEP_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.feature = feature;
      this.iterationNumber = iterationNumber;
      this.scenario = scenario;
      this.scenarioSetupStep = scenarioSetupStep;
      this.result = result;
   }
}
