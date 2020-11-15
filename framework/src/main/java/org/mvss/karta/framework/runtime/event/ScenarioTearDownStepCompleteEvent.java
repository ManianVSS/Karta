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
public class ScenarioTearDownStepCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private int               iterationNumber;
   private String            scenarioName;
   private TestStep          scenarioTearDownStep;
   private StepResult        result;

   public ScenarioTearDownStepCompleteEvent( String runName, String featureName, int iterationNumber, String scenarioName, TestStep scenarioTearDownStep, StepResult result )
   {
      super( StandardEventsTypes.SCENARIO_TEARDOWN_STEP_COMPLETE_EVENT, runName );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenarioName;
      this.scenarioTearDownStep = scenarioTearDownStep;
      this.result = result;
   }

   @Builder
   public ScenarioTearDownStepCompleteEvent( String runName, UUID id, Date timeOfOccurrence, String featureName, int iterationNumber, String scenarioName, TestStep scenarioTearDownStep, StepResult result )
   {
      super( StandardEventsTypes.SCENARIO_TEARDOWN_STEP_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenarioName;
      this.scenarioTearDownStep = scenarioTearDownStep;
      this.result = result;
   }
}
