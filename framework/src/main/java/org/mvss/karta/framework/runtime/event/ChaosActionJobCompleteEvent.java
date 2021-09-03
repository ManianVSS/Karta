package org.mvss.karta.framework.runtime.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.ParserUtils;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
@NoArgsConstructor
public class ChaosActionJobCompleteEvent extends JobEvent
{
   private static final long serialVersionUID = 1L;

   public ChaosActionJobCompleteEvent( Event event )
   {
      super( event );
      parameters.put( Constants.CHAOS_ACTION,
               ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.CHAOS_ACTION ), ChaosAction.class ) );
      parameters.put( Constants.RESULT, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.RESULT ), StepResult.class ) );
   }

   public ChaosActionJobCompleteEvent( String runName, String featureName, TestJob job, long iterationNumber, ChaosAction chaosAction,
                                       StepResult result )
   {
      super( StandardEventsTypes.CHAOS_ACTION_JOB_COMPLETE_EVENT, runName, featureName, job, iterationNumber );
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
