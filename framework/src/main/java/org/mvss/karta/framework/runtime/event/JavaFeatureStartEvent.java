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
public class JavaFeatureStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;

   public JavaFeatureStartEvent( String runName, String featureName )
   {
      super( StandardEventsTypes.FEATURE_START_EVENT, runName );
      this.featureName = featureName;
   }

   @Builder
   public JavaFeatureStartEvent( String runName, UUID id, Date timeOfOccurrence, String featureName )
   {
      super( StandardEventsTypes.FEATURE_START_EVENT, runName, id, timeOfOccurrence );
      this.featureName = featureName;
   }
}
