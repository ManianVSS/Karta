package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.TestFeature;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
@NoArgsConstructor
public class FeatureStartEvent extends FeatureEvent
{
   private static final long serialVersionUID = 1L;

   public FeatureStartEvent( Event event )
   {
      super( event );
   }

   public FeatureStartEvent( String runName, TestFeature feature )
   {
      super( StandardEventsTypes.FEATURE_START_EVENT, runName, feature );
   }
}
