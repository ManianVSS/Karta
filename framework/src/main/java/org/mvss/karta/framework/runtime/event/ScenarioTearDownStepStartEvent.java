package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.PreparedStep;
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
public class ScenarioTearDownStepStartEvent extends ScenarioEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public ScenarioTearDownStepStartEvent( String runName, String featureName, long iterationNumber, String scenarioName, PreparedStep step )
   {
      super( StandardEventsTypes.SCENARIO_TEARDOWN_STEP_START_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.STEP, step );
   }

   @JsonIgnore
   public PreparedStep getStep()
   {
      return (PreparedStep) parameters.get( Constants.STEP );
   }
}
