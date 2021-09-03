package org.mvss.karta.framework.runtime.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.ParserUtils;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
@NoArgsConstructor
public class JavaScenarioSetupCompleteEvent extends ScenarioEvent
{
   private static final long serialVersionUID = 1L;

   public JavaScenarioSetupCompleteEvent( Event event )
   {
      super( event );
      parameters.put( Constants.STEP_IDENTIFIER,
               ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.STEP_IDENTIFIER ), String.class ) );
      parameters.put( Constants.RESULT, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.RESULT ), StepResult.class ) );
   }

   public JavaScenarioSetupCompleteEvent( String runName, String featureName, long iterationNumber, String scenarioName, String stepIdentifier,
                                          StepResult result )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_SETUP_COMPLETE_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.STEP_IDENTIFIER, stepIdentifier );
      this.parameters.put( Constants.RESULT, result );
   }

   @JsonIgnore
   public String getStepIdentifier()
   {
      return parameters.get( Constants.STEP_IDENTIFIER ).toString();
   }

   @JsonIgnore
   public StepResult getResult()
   {
      return (StepResult) parameters.get( Constants.RESULT );
   }
}
