package org.mvss.karta.framework.runtime.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class JavaScenarioStartEvent extends ScenarioEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public JavaScenarioStartEvent( String runName, String featureName, long iterationNumber, String scenarioName )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_START_EVENT, runName, featureName, iterationNumber, scenarioName );
   }

}
