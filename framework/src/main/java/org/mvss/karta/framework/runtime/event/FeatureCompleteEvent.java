package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.TestFeature;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class FeatureCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private TestFeature       feature;

   private FeatureResult     result;

   public FeatureCompleteEvent( String runName, TestFeature feature, FeatureResult result )
   {
      super( StandardEventsTypes.FEATURE_COMPLETE_EVENT, runName );
      this.feature = feature;
      this.result = result;
   }

   @Builder
   public FeatureCompleteEvent( String runName, UUID id, Date timeOfOccurrence, TestFeature feature, FeatureResult result )
   {
      super( StandardEventsTypes.FEATURE_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.feature = feature;
      this.result = result;
   }
}
