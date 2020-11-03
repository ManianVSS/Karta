package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.core.ScenarioResult;
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
public class ScenarioCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private TestFeature       feature;
   private int               iterationNumber;
   private TestScenario      scenario;
   private ScenarioResult    result;

   public ScenarioCompleteEvent( String runName, TestFeature feature, int iterationNumber, TestScenario scenario, ScenarioResult result )
   {
      super( StandardEventsTypes.SCENARIO_COMPLETE_EVENT, runName );
      this.feature = feature;
      this.iterationNumber = iterationNumber;
      this.scenario = scenario;
      this.result = result;
   }

   @Builder
   public ScenarioCompleteEvent( String runName, UUID id, Date timeOfOccurrence, TestFeature feature, int iterationNumber, TestScenario scenario, ScenarioResult result )
   {
      super( StandardEventsTypes.SCENARIO_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.feature = feature;
      this.iterationNumber = iterationNumber;
      this.scenario = scenario;
      this.result = result;
   }
}
