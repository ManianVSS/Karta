package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

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
public class ScenarioSetupStepStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private int               iterationNumber;
   private String            scenarioName;
   private TestStep          scenarioSetupStep;

   public ScenarioSetupStepStartEvent( String runName, String featureName, int iterationNumber, String scenarioName, TestStep scenarioSetupStep )
   {
      super( StandardEventsTypes.SCENARIO_SETUP_STEP_START_EVENT, runName );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenarioName;
      this.scenarioSetupStep = scenarioSetupStep;
   }

   @Builder
   public ScenarioSetupStepStartEvent( String runName, UUID id, Date timeOfOccurrence, String feature, int iterationNumber, String scenario, TestStep scenarioSetupStep )
   {
      super( StandardEventsTypes.SCENARIO_SETUP_STEP_START_EVENT, runName, id, timeOfOccurrence );
      this.featureName = feature;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenario;
      this.scenarioSetupStep = scenarioSetupStep;
   }
}
