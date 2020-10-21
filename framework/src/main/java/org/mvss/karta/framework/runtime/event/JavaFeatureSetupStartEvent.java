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
public class JavaFeatureSetupStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private String            methodName;

   public JavaFeatureSetupStartEvent( String runName, String featureName, String methodName )
   {
      super( StandardEventsTypes.FEATURE_SETUP_STEP_START_EVENT, runName );
      this.featureName = featureName;
      this.methodName = methodName;
   }

   @Builder
   public JavaFeatureSetupStartEvent( String runName, UUID id, Date timeOfOccurrence, String featureName, String methodName )
   {
      super( StandardEventsTypes.FEATURE_SETUP_STEP_START_EVENT, runName, id, timeOfOccurrence );
      this.featureName = featureName;
      this.methodName = methodName;
   }
}
