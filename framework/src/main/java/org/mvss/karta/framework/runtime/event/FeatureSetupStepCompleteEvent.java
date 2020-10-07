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
public class FeatureSetupStepCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private TestFeature       feature;
   private TestStep          setupStep;
   private StepResult        result;

   public FeatureSetupStepCompleteEvent( String runName, TestFeature feature, TestStep setupStep, StepResult result )
   {
      super( StandardEventsTypes.FEATURE_SETUP_STEP_COMPLETE_EVENT, runName );
      this.feature = feature;
      this.setupStep = setupStep;
      this.result = result;
   }

   @Builder
   public FeatureSetupStepCompleteEvent( String runName, UUID id, Date timeOfOccurrence, TestFeature feature, TestStep setupStep, StepResult result )
   {
      super( StandardEventsTypes.FEATURE_SETUP_STEP_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.feature = feature;
      this.setupStep = setupStep;
      this.result = result;
   }
}
