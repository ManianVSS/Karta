package org.mvss.karta.framework.runtime.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
@NoArgsConstructor
public class JavaFeatureStartEvent extends FeatureEvent
{
   private static final long serialVersionUID = 1L;

   public JavaFeatureStartEvent( Event event )
   {
      super( event );
   }

   public JavaFeatureStartEvent( String runName, String featureName )
   {
      super( StandardEventsTypes.JAVA_FEATURE_START_EVENT, runName, featureName );
   }
}
