package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.PreparedStep;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
public class ScenarioSetupStepStartEvent extends ScenarioEvent
{
   private static final long serialVersionUID = 1L;

   public ScenarioSetupStepStartEvent( Event event )
   {
      super( event );
      parameters.put( Constants.STEP, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.STEP ), PreparedStep.class ) );
   }

   public ScenarioSetupStepStartEvent( String runName, String featureName, long iterationNumber, String scenarioName, PreparedStep step )
   {
      super( StandardEventsTypes.SCENARIO_SETUP_STEP_START_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.STEP, step );
   }

   @JsonIgnore
   public PreparedStep getStep()
   {
      return (PreparedStep) parameters.get( Constants.STEP );
   }
}
