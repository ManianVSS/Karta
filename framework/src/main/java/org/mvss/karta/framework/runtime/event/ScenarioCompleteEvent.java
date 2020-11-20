package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.TestScenario;
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
public class ScenarioCompleteEvent extends ScenarioEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public ScenarioCompleteEvent( String runName, String featureName, long iterationNumber, TestScenario scenario, ScenarioResult result )
   {
      super( StandardEventsTypes.SCENARIO_COMPLETE_EVENT, runName, featureName, iterationNumber, scenario );
      this.parameters.put( Constants.RESULT, result );
   }

   @JsonIgnore
   public ScenarioResult getResult()
   {
      return (ScenarioResult) parameters.get( Constants.RESULT );
   }
}
