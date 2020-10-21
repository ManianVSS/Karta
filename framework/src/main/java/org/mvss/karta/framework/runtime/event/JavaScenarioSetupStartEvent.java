package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class JavaScenarioSetupStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private long              iterationNumber;
   private String            method;
   private String            scenarioName;

   public JavaScenarioSetupStartEvent( String runName, String feature, long iterationNumber, String method, String scenarioName )
   {
      super( StandardEventsTypes.SCENARIO_SETUP_STEP_START_EVENT, runName );
      this.featureName = feature;
      this.iterationNumber = iterationNumber;
      this.method = method;
      this.scenarioName = scenarioName;
   }

   @Builder
   public JavaScenarioSetupStartEvent( String runName, UUID id, Date timeOfOccurrence, String feature, long iterationNumber, String method, String scenarioName )
   {
      super( StandardEventsTypes.SCENARIO_SETUP_STEP_START_EVENT, runName, id, timeOfOccurrence );
      this.featureName = feature;
      this.iterationNumber = iterationNumber;
      this.method = method;
      this.scenarioName = scenarioName;
   }
}
