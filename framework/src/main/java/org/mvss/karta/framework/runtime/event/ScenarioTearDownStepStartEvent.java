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
public class ScenarioTearDownStepStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private int               iterationNumber;
   private String            scenarioName;
   private TestStep          scenarioTearDownStep;

   public ScenarioTearDownStepStartEvent( String runName, String featureName, int iterationNumber, String scenarioName, TestStep scenarioTearDownStep )
   {
      super( StandardEventsTypes.SCENARIO_TEARDOWN_STEP_START_EVENT, runName );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenarioName;
      this.scenarioTearDownStep = scenarioTearDownStep;
   }

   @Builder
   public ScenarioTearDownStepStartEvent( String runName, UUID id, Date timeOfOccurrence, String featureName, int iterationNumber, String scenarioName, TestStep scenarioTearDownStep )
   {
      super( StandardEventsTypes.SCENARIO_TEARDOWN_STEP_START_EVENT, runName, id, timeOfOccurrence );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenarioName;
      this.scenarioTearDownStep = scenarioTearDownStep;
   }
}
