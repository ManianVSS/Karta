package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.PreparedChaosAction;
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
public class ScenarioChaosActionStartEvent extends ScenarioEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public ScenarioChaosActionStartEvent( String runName, String featureName, long iterationNumber, String scenarioName, PreparedChaosAction chaosAction )
   {
      super( StandardEventsTypes.SCENARIO_CHAOS_ACTION_START_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.CHAOS_ACTION, chaosAction );
   }

   @JsonIgnore
   public PreparedChaosAction getPreparedChaosAction()
   {
      return (PreparedChaosAction) parameters.get( Constants.CHAOS_ACTION );
   }
}
