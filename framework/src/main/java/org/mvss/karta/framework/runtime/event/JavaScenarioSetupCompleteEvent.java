package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.core.StepResult;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class JavaScenarioSetupCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private long              iterationNumber;
   private String            method;
   private String            scenarioName;
   private StepResult        result;

   public JavaScenarioSetupCompleteEvent( String runName, String featureName, long iterationNumber, String method, String scenarioName, StepResult result )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_SETUP_COMPLETE_EVENT, runName );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.method = method;
      this.scenarioName = scenarioName;
      this.result = result;
   }

   @Builder
   public JavaScenarioSetupCompleteEvent( String runName, UUID id, Date timeOfOccurrence, String featureName, long iterationNumber, String method, String scenarioName, StepResult result )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_SETUP_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.method = method;
      this.scenarioName = scenarioName;
      this.result = result;
   }
}