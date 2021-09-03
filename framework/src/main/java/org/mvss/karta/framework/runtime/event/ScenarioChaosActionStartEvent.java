package org.mvss.karta.framework.runtime.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mvss.karta.framework.core.PreparedChaosAction;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.ParserUtils;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
@NoArgsConstructor
public class ScenarioChaosActionStartEvent extends ScenarioEvent
{
   private static final long serialVersionUID = 1L;

   public ScenarioChaosActionStartEvent( Event event )
   {
      super( event );
      parameters.put( Constants.CHAOS_ACTION,
               ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.CHAOS_ACTION ), PreparedChaosAction.class ) );
   }

   public ScenarioChaosActionStartEvent( String runName, String featureName, long iterationNumber, String scenarioName,
                                         PreparedChaosAction chaosAction )
   {
      super( StandardEventsTypes.SCENARIO_CHAOS_ACTION_START_EVENT, runName, featureName, iterationNumber, scenarioName );
      this.parameters.put( Constants.CHAOS_ACTION, chaosAction );
   }

   @JsonIgnore
   public PreparedChaosAction getPreparedChaosAction()
   {
      return (PreparedChaosAction) parameters.get( Constants.CHAOS_ACTION );
   }
}
