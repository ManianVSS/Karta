package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.core.StepResult;
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

   private String            featureName;
   private int               iterationNumber;
   private String            scenarioName;
   private TestStep          scenarioSetupStep;
   private StepResult        result;

   public ScenarioSetupStepCompleteEvent( String runName, String featureName, int iterationNumber, String scenarioName, TestStep scenarioSetupStep, StepResult result )
   {
      super( StandardEventsTypes.SCENARIO_SETUP_STEP_COMPLETE_EVENT, runName );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenarioName;
      this.scenarioSetupStep = scenarioSetupStep;
      this.result = result;
   }

   @Builder
   public ScenarioSetupStepCompleteEvent( String runName, UUID id, Date timeOfOccurrence, String featureName, int iterationNumber, String scenarioName, TestStep scenarioSetupStep, StepResult result )
   {
      super( StandardEventsTypes.SCENARIO_SETUP_STEP_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenarioName;
      this.scenarioSetupStep = scenarioSetupStep;
      this.result = result;
   }
}
