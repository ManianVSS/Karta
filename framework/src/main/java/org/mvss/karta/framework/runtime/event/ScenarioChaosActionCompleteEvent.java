package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.StepResult;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class ScenarioChaosActionCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private int               iterationNumber;
   private String            scenarioName;
   private ChaosAction       chaosAction;
   private StepResult        result;

   public ScenarioChaosActionCompleteEvent( String runName, String feature, int iterationNumber, String scenario, ChaosAction chaosAction, StepResult result )
   {
      super( StandardEventsTypes.SCENARIO_CHAOS_ACTION_COMPLETE_EVENT, runName );
      this.featureName = feature;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenario;
      this.chaosAction = chaosAction;
      this.result = result;
   }

   @Builder
   public ScenarioChaosActionCompleteEvent( String runName, UUID id, Date timeOfOccurrence, String feature, int iterationNumber, String scenario, ChaosAction chaosAction, StepResult result )
   {
      super( StandardEventsTypes.SCENARIO_CHAOS_ACTION_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.featureName = feature;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenario;
      this.chaosAction = chaosAction;
      this.result = result;
   }
}
