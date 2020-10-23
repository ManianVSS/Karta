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
public class JavaFeatureCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;

   public JavaFeatureCompleteEvent( String runName, String featureName )
   {
      super( StandardEventsTypes.JAVA_FEATURE_COMPLETE_EVENT, runName );
      this.featureName = featureName;
   }

   @Builder
   public JavaFeatureCompleteEvent( String runName, UUID id, Date timeOfOccurrence, String featureName )
   {
      super( StandardEventsTypes.JAVA_FEATURE_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.featureName = featureName;
   }
}
