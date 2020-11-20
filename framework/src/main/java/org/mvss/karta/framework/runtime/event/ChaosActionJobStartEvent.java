package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.runtime.Constants;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class ChaosActionJobStartEvent extends JobEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

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
