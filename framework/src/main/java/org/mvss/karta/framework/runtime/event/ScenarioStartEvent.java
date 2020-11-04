package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.core.TestScenario;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class ScenarioStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private int               iterationNumber;
   private TestScenario      scenario;

   public ScenarioStartEvent( String runName, String featureName, int iterationNumber, TestScenario scenario )
   {
      super( StandardEventsTypes.SCENARIO_START_EVENT, runName );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.scenario = scenario;
   }

   @Builder
   public ScenarioStartEvent( String runName, UUID id, Date timeOfOccurrence, String featureName, int iterationNumber, TestScenario scenario )
   {
      super( StandardEventsTypes.SCENARIO_START_EVENT, runName, id, timeOfOccurrence );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.scenario = scenario;
   }
}
