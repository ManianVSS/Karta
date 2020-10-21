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
public class JavaFeatureTearDownCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureName;
   private String            method;
   private StepResult        result;

   public JavaFeatureTearDownCompleteEvent( String runName, String featureName, String method, StepResult result )
   {
      super( StandardEventsTypes.FEATURE_SETUP_STEP_COMPLETE_EVENT, runName );
      this.featureName = featureName;
      this.method = method;
      this.result = result;
   }

   @Builder
   public JavaFeatureTearDownCompleteEvent( String runName, UUID id, Date timeOfOccurrence, String featureName, String method, StepResult result )
   {
      super( StandardEventsTypes.FEATURE_SETUP_STEP_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.featureName = featureName;
      this.method = method;
      this.result = result;
   }
}
