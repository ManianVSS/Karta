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
public class ScenarioStepStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private int               iterationNumber;
   private String            scenarioName;
   private TestStep          scenarioStep;

   public ScenarioStepStartEvent( String runName, String featureName, int iterationNumber, String scenarioName, TestStep scenarioStep )
   {
      super( StandardEventsTypes.SCENARIO_STEP_START_EVENT, runName );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenarioName;
      this.scenarioStep = scenarioStep;
   }

   @Builder
   public ScenarioStepStartEvent( String runName, UUID id, Date timeOfOccurrence, String featureName, int iterationNumber, String scenarioName, TestStep scenarioStep )
   {
      super( StandardEventsTypes.SCENARIO_STEP_START_EVENT, runName, id, timeOfOccurrence );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenarioName;
      this.scenarioStep = scenarioStep;
   }
}
