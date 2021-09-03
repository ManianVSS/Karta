package org.mvss.karta.framework.runtime.event;

import lombok.*;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
@NoArgsConstructor
public class JavaScenarioStartEvent extends ScenarioEvent
{
   private static final long serialVersionUID = 1L;

   public JavaScenarioStartEvent( Event event )
   {
      super( event );
   }

   public JavaScenarioStartEvent( String runName, String featureName, long iterationNumber, String scenarioName )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_START_EVENT, runName, featureName, iterationNumber, scenarioName );
   }
}
