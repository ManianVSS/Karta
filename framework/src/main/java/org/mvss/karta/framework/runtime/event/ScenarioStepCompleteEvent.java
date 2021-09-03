package org.mvss.karta.framework.runtime.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mvss.karta.framework.core.PreparedStep;
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
public class ScenarioStepCompleteEvent extends ScenarioEvent
{
   private static final long serialVersionUID = 1L;

   public ScenarioStepCompleteEvent( Event event )
   {
      super( event );
      parameters.put( Constants.STEP, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.STEP ), PreparedStep.class ) );
      parameters.put( Constants.RESULT, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.RESULT ), StepResult.class ) );
   }

   public ScenarioStepCompleteEvent( String runName, String featureName, long iterationNumber, String scenarioName, PreparedStep step,
                                     StepResult result )
   {
      super( StandardEventsTypes.SCENARIO_STEP_COMPLETE_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.STEP, step );
      this.parameters.put( Constants.RESULT, result );
   }

   @JsonIgnore
   public PreparedStep getStep()
   {
      return (PreparedStep) parameters.get( Constants.STEP );
   }

   @JsonIgnore
   public StepResult getResult()
   {
      return (StepResult) parameters.get( Constants.RESULT );
   }
}
