package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestStep;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class FeatureTearDownStepCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private TestFeature       feature;
   private TestStep          tearDownStep;
   private StepResult        result;

   public FeatureTearDownStepCompleteEvent( String runName, TestFeature feature, TestStep tearDownStep, StepResult result )
   {
      super( StandardEventsTypes.FEATURE_TEARDOWN_STEP_COMPLETE_EVENT, runName );
      this.feature = feature;
      this.tearDownStep = tearDownStep;
      this.result = result;
   }

   @Builder
   public FeatureTearDownStepCompleteEvent( String runName, UUID id, Date timeOfOccurrence, TestFeature feature, TestStep tearDownStep, StepResult result )
   {
      super( StandardEventsTypes.FEATURE_TEARDOWN_STEP_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.feature = feature;
      this.tearDownStep = tearDownStep;
      this.result = result;
   }
}
