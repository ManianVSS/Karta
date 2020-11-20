package org.mvss.karta.framework.runtime.event;

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
public class JavaScenarioTearDownCompleteEvent extends ScenarioEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public JavaScenarioTearDownCompleteEvent( String runName, String featureName, long iterationNumber, String scenarioName, String stepIdentifier, StepResult result )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_TEARDOWN_COMPLETE_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.STEP_IDENTIFIER, stepIdentifier );
      this.parameters.put( Constants.RESULT, result );
   }

   @JsonIgnore
   public String getStepIdentifier()
   {
      return parameters.get( Constants.STEP_IDENTIFIER ).toString();
   }

   @JsonIgnore
   public StepResult getResult()
   {
      return (StepResult) parameters.get( Constants.RESULT );
   }
}
