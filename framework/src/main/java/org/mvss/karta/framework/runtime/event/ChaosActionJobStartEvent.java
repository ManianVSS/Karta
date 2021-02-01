package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.TestJob;
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
public class ChaosActionJobStartEvent extends JobEvent
{
   private static final long serialVersionUID = 1L;

   public ChaosActionJobStartEvent( Event event )
   {
      super( event );
      parameters.put( Constants.CHAOS_ACTION, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.CHAOS_ACTION ), ChaosAction.class ) );
   }

   public ChaosActionJobStartEvent( String runName, String featureName, TestJob job, long iterationNumber, ChaosAction chaosAction )
   {
      super( StandardEventsTypes.CHAOS_ACTION_JOB_START_EVENT, runName, featureName, job, iterationNumber );
      this.parameters.put( Constants.CHAOS_ACTION, chaosAction );
   }

   @JsonIgnore
   public ChaosAction getChaosAction()
   {
      return (ChaosAction) parameters.get( Constants.CHAOS_ACTION );
   }
}
