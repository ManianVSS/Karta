package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.chaos.ChaosAction;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class ScenarioChaosActionStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private int               iterationNumber;
   private String            scenarioName;
   private ChaosAction       chaosAction;

   public ScenarioChaosActionStartEvent( String runName, String featureName, int iterationNumber, String scenarioName, ChaosAction chaosAction )
   {
      super( StandardEventsTypes.SCENARIO_CHAOS_ACTION_START_EVENT, runName );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenarioName;
      this.chaosAction = chaosAction;
   }

   @Builder
   public ScenarioChaosActionStartEvent( String runName, UUID id, Date timeOfOccurrence, String featureName, int iterationNumber, String scenarioName, ChaosAction chaosAction )
   {
      super( StandardEventsTypes.SCENARIO_CHAOS_ACTION_START_EVENT, runName, id, timeOfOccurrence );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenarioName;
      this.chaosAction = chaosAction;
   }
}
