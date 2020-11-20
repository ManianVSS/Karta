package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.ScenarioResult;
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
public class JavaScenarioCompleteEvent extends ScenarioEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public JavaScenarioCompleteEvent( String runName, String featureName, long iterationNumber, String scenarioName, ScenarioResult result )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_COMPLETE_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.RESULT, result );
   }

   @JsonIgnore
   public ScenarioResult getResult()
   {
      return (ScenarioResult) parameters.get( Constants.RESULT );
   }
}
