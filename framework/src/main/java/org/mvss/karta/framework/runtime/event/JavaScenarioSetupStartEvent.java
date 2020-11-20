package org.mvss.karta.framework.runtime.event;

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
public class JavaScenarioSetupStartEvent extends ScenarioEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public JavaScenarioSetupStartEvent( String runName, String featureName, long iterationNumber, String scenarioName, String stepIdentifier )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_SETUP_START_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.STEP_IDENTIFIER, stepIdentifier );
   }

   @JsonIgnore
   public String getStepIdentifier()
   {
      return parameters.get( Constants.STEP_IDENTIFIER ).toString();
   }
}
