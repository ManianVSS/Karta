package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.chaos.ChaosAction;
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
public class JavaScenarioChaosActionCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private long              iterationNumber;
   private String            scenarioName;
   private ChaosAction       chaosAction;
   private StepResult        result;

   public JavaScenarioChaosActionCompleteEvent( String runName, String featureName, long iterationNumber, String scenarioName, ChaosAction chaosAction, StepResult result )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_CHAOS_ACTION_COMPLETE_EVENT, runName );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenarioName;
      this.chaosAction = chaosAction;
      this.result = result;
   }

   @Builder
   public JavaScenarioChaosActionCompleteEvent( String runName, UUID id, Date timeOfOccurrence, String featureName, long iterationNumber, String scenarioName, ChaosAction chaosAction, StepResult result )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_CHAOS_ACTION_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.featureName = featureName;
      this.iterationNumber = iterationNumber;
      this.scenarioName = scenarioName;
      this.chaosAction = chaosAction;
      this.result = result;
   }
}
