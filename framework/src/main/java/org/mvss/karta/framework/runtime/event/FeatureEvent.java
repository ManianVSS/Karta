package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.TestFeature;
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
public abstract class FeatureEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public FeatureEvent( String eventName, String runName, String featureName )
   {
      super( eventName, runName );
      this.parameters.put( Constants.FEATURE, TestFeature.builder().name( featureName ).build() );
      this.parameters.put( Constants.FEATURE_NAME, featureName );
   }

   public FeatureEvent( String eventName, String runName, TestFeature feature )
   {
      super( eventName, runName );
      this.parameters.put( Constants.FEATURE, feature );
      this.parameters.put( Constants.FEATURE_NAME, feature.getName() );
   }

   @JsonIgnore
   public TestFeature getFeature()
   {
      return (TestFeature) parameters.get( Constants.FEATURE );
   }

   @JsonIgnore
   public String getFeatureName()
   {
      return parameters.get( Constants.FEATURE_NAME ).toString();
   }
}
