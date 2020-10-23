package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.core.TestFeature;
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

   private TestFeature       feature;
   private int               iterationNumber;
   private TestScenario      scenario;

   public ScenarioStartEvent( String runName, TestFeature feature, int iterationNumber, TestScenario scenario )
   {
      super( StandardEventsTypes.SCENARIO_START_EVENT, runName );
      this.feature = feature;
      this.iterationNumber = iterationNumber;
      this.scenario = scenario;
   }

   @Builder
   public ScenarioStartEvent( String runName, UUID id, Date timeOfOccurrence, TestFeature feature, int iterationNumber, TestScenario scenario )
   {
      super( StandardEventsTypes.SCENARIO_START_EVENT, runName, id, timeOfOccurrence );
      this.feature = feature;
      this.iterationNumber = iterationNumber;
      this.scenario = scenario;
   }
}
