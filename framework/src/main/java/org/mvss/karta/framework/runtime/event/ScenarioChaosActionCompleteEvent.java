package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.runtime.Constants;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class ScenarioChaosActionCompleteEvent extends ScenarioEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public ScenarioChaosActionCompleteEvent( String runName, String featureName, long iterationNumber, String scenarioName, ChaosAction chaosAction, StepResult result )
   {
      super( StandardEventsTypes.SCENARIO_CHAOS_ACTION_COMPLETE_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.CHAOS_ACTION, chaosAction );
      this.parameters.put( Constants.RESULT, result );
   }

   @JsonIgnore
   public ChaosAction getChaosAction()
   {
      return (ChaosAction) parameters.get( Constants.CHAOS_ACTION );
   }

   @JsonIgnore
   public StepResult getResult()
   {
      return (StepResult) parameters.get( Constants.RESULT );
   }
}
