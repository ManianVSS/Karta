package org.mvss.karta.framework.runtime.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mvss.karta.framework.chaos.ChaosAction;
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
public class JavaScenarioChaosActionCompleteEvent extends ScenarioEvent
{
   private static final long serialVersionUID = 1L;

   public JavaScenarioChaosActionCompleteEvent( Event event )
   {
      super( event );
      parameters.put( Constants.CHAOS_ACTION,
               ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.CHAOS_ACTION ), ChaosAction.class ) );
      parameters.put( Constants.RESULT, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.RESULT ), StepResult.class ) );
   }

   public JavaScenarioChaosActionCompleteEvent( String runName, String featureName, long iterationNumber, String scenarioName,
                                                ChaosAction chaosAction, StepResult result )
   {
      super( StandardEventsTypes.JAVA_SCENARIO_CHAOS_ACTION_COMPLETE_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.CHAOS_ACTION, chaosAction );
      this.parameters.put( Constants.RESULT, result );
   }

   @JsonIgnore
   public ChaosAction getChaosAction()
   {
      return (ChaosAction) parameters.get( Constants.CHAOS_ACTION );
   }

   @JsonIgnore
   public StepResult getResult()
   {
      return (StepResult) parameters.get( Constants.RESULT );
   }
}
