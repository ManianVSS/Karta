package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.TestStep;
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
public class ScenarioStepStartEvent extends ScenarioEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public ScenarioStepStartEvent( String runName, String featureName, long iterationNumber, String scenarioName, TestStep step )
   {
      super( StandardEventsTypes.SCENARIO_STEP_START_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.STEP, step );
   }

   @JsonIgnore
   public TestStep getStep()
   {
      return (TestStep) parameters.get( Constants.STEP );
   }
}
