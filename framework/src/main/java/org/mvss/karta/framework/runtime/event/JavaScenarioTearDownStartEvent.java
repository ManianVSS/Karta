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
public class JavaScenarioTearDownStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private long              iterationNumber;
   private String            method;
   private String            scenarioName;

   public JavaScenarioTearDownStartEvent( String runName, String featureName, long iterationNumber, String method, String scenarioName )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_TEARDOWN_START_EVENT, runName );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.method = method;
      this.scenarioName = scenarioName;
   }

   @Builder
   public JavaScenarioTearDownStartEvent( String runName, UUID id, Date timeOfOccurrence, String featureName, long iterationNumber, String method, String scenarioName )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_TEARDOWN_START_EVENT, runName, id, timeOfOccurrence );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.method = method;
      this.scenarioName = scenarioName;
   }
}
