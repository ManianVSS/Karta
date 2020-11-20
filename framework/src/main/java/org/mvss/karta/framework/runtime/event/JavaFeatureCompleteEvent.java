package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.FeatureResult;
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
public class JavaFeatureCompleteEvent extends FeatureEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public JavaFeatureCompleteEvent( String runName, String featureName, FeatureResult result )
   {
      super( StandardEventsTypes.JAVA_FEATURE_COMPLETE_EVENT, runName, featureName );
      this.parameters.put( Constants.RESULT, result );
   }

   @JsonIgnore
   public FeatureResult getResult()
   {
      return (FeatureResult) parameters.get( Constants.RESULT );
   }
}
