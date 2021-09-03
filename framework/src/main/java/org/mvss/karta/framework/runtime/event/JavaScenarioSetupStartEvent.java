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
public class JavaScenarioSetupStartEvent extends ScenarioEvent
{
   private static final long serialVersionUID = 1L;

   public JavaScenarioSetupStartEvent( Event event )
   {
      super( event );
      parameters.put( Constants.STEP_IDENTIFIER,
               ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.STEP_IDENTIFIER ), String.class ) );
   }

   public JavaScenarioSetupStartEvent( String runName, String featureName, long iterationNumber, String scenarioName, String stepIdentifier )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_SETUP_START_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.STEP_IDENTIFIER, stepIdentifier );
   }

   @JsonIgnore
   public String getStepIdentifier()
   {
      return parameters.get( Constants.STEP_IDENTIFIER ).toString();
   }
}
