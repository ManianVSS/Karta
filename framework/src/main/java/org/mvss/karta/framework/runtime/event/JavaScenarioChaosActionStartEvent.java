package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.chaos.ChaosAction;
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
public class JavaScenarioChaosActionStartEvent extends ScenarioEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public JavaScenarioChaosActionStartEvent( String runName, String featureName, long iterationNumber, String scenarioName, ChaosAction chaosAction )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_CHAOS_ACTION_START_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.CHAOS_ACTION, chaosAction );
   }

   @JsonIgnore
   public ChaosAction getChaosAction()
   {
      return (ChaosAction) parameters.get( Constants.CHAOS_ACTION );
   }
}
