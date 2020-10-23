package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

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
public class JavaFeatureSetupCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private String            methodName;
   private StepResult        result;

   public JavaFeatureSetupCompleteEvent( String runName, String featureName, String methodName, StepResult result )
   {
      super( StandardEventsTypes.JAVA_FEATURE_SETUP_COMPLETE_EVENT, runName );
      this.featureName = featureName;
      this.methodName = methodName;
      this.result = result;
   }

   @Builder
   public JavaFeatureSetupCompleteEvent( String runName, UUID id, Date timeOfOccurrence, String featureName, String methodName, StepResult result )
   {
      super( StandardEventsTypes.JAVA_FEATURE_SETUP_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.featureName = featureName;
      this.methodName = methodName;
      this.result = result;
   }
}
