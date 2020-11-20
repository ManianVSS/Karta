package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.runtime.Constants;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class JavaFeatureTearDownStartEvent extends FeatureEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public JavaFeatureTearDownStartEvent( String runName, String featureName, String stepIdentifier )
   {
      super( StandardEventsTypes.JAVA_FEATURE_TEARDOWN_START_EVENT, runName, featureName );
      this.parameters.put( Constants.STEP_IDENTIFIER, stepIdentifier );
   }

   @JsonIgnore
   public String getStepIdentifier()
   {
      return parameters.get( Constants.STEP_IDENTIFIER ).toString();
   }
}
