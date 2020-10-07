package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.chaos.ChaosAction;
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
public class ScenarioChaosActionStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private TestFeature       feature;
   private long              iterationNumber;
   private TestScenario      scenario;
   private ChaosAction       chaosAction;

   public ScenarioChaosActionStartEvent( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, ChaosAction chaosAction )
   {
      super( StandardEventsTypes.SCENARIO_CHAOS_ACTION_START_EVENT, runName );
      this.feature = feature;
      this.iterationNumber = iterationNumber;
      this.scenario = scenario;
      this.chaosAction = chaosAction;
   }

   @Builder
   public ScenarioChaosActionStartEvent( String runName, UUID id, Date timeOfOccurrence, TestFeature feature, long iterationNumber, TestScenario scenario, ChaosAction chaosAction )
   {
      super( StandardEventsTypes.SCENARIO_CHAOS_ACTION_START_EVENT, runName, id, timeOfOccurrence );
      this.feature = feature;
      this.iterationNumber = iterationNumber;
      this.scenario = scenario;
      this.chaosAction = chaosAction;
   }
}
