package org.mvss.karta.framework.runtime.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.ParserUtils;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
@NoArgsConstructor
public class JavaScenarioTearDownStartEvent extends ScenarioEvent
{
   private static final long serialVersionUID = 1L;

   public JavaScenarioTearDownStartEvent( Event event )
   {
      super( event );
      parameters.put( Constants.STEP_IDENTIFIER,
               ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.STEP_IDENTIFIER ), String.class ) );
   }

   public JavaScenarioTearDownStartEvent( String runName, String featureName, long iterationNumber, String scenarioName, String stepIdentifier )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_TEARDOWN_START_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.STEP_IDENTIFIER, stepIdentifier );
   }

   @JsonIgnore
   public String getStepIdentifier()
   {
      return parameters.get( Constants.STEP_IDENTIFIER ).toString();
   }
}
