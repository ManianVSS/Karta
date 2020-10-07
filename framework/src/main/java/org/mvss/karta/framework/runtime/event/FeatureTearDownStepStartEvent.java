package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

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
public class FeatureTearDownStepStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private TestFeature       feature;
   private TestStep          tearDownStep;

   public FeatureTearDownStepStartEvent( String runName, TestFeature feature, TestStep tearDownStep )
   {
      super( StandardEventsTypes.FEATURE_TEARDOWN_STEP_START_EVENT, runName );
      this.feature = feature;
      this.tearDownStep = tearDownStep;
   }

   @Builder
   public FeatureTearDownStepStartEvent( String runName, UUID id, Date timeOfOccurrence, TestFeature feature, TestStep tearDownStep )
   {
      super( StandardEventsTypes.FEATURE_TEARDOWN_STEP_START_EVENT, runName, id, timeOfOccurrence );
      this.feature = feature;
      this.tearDownStep = tearDownStep;
   }
}
