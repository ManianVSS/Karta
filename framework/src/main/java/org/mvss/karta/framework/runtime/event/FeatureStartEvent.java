package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.TestFeature;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class FeatureStartEvent extends FeatureEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public FeatureStartEvent( String runName, TestFeature feature )
   {
      super( StandardEventsTypes.FEATURE_START_EVENT, runName, feature );
   }
}
