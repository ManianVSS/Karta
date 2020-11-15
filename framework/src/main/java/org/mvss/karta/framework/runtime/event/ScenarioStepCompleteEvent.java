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
public class ScenarioStepCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private int               iterationNumber;
   private String            scenarioName;
   private TestStep          scenarioStep;
   private StepResult        result;

   public ScenarioStepCompleteEvent( String runName, String feature, int iterationNumber, String scenarioName, TestStep scenarioStep, StepResult result )
   {
      super( StandardEventsTypes.SCENARIO_STEP_COMPLETE_EVENT, runName );
      this.featureName = feature;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenarioName;
      this.scenarioStep = scenarioStep;
      this.result = result;
   }

   @Builder
   public ScenarioStepCompleteEvent( String runName, UUID id, Date timeOfOccurrence, String feature, int iterationNumber, String scenarioName, TestStep scenarioStep, StepResult result )
   {
      super( StandardEventsTypes.SCENARIO_STEP_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.featureName = feature;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenarioName;
      this.scenarioStep = scenarioStep;
      this.result = result;
   }
}
