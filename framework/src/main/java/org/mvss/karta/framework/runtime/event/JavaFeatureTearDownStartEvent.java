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
public class JavaFeatureTearDownStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private String            method;

   public JavaFeatureTearDownStartEvent( String runName, String featureName, String methodName )
   {
      super( StandardEventsTypes.JAVA_FEATURE_TEARDOWN_START_EVENT, runName );
      this.featureName = featureName;
      this.method = methodName;
   }

   @Builder
   public JavaFeatureTearDownStartEvent( String runName, UUID id, Date timeOfOccurrence, String featureName, String methodName )
   {
      super( StandardEventsTypes.JAVA_FEATURE_TEARDOWN_START_EVENT, runName, id, timeOfOccurrence );
      this.featureName = featureName;
      this.method = methodName;
   }
}
