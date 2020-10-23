package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.TestJob;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class ChaosActionJobStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private TestJob           job;
   private int               iterationNumber;
   private ChaosAction       chaosAction;

   public ChaosActionJobStartEvent( String runName, TestJob job, int iterationNumber, ChaosAction chaosAction )
   {
      super( StandardEventsTypes.SCENARIO_CHAOS_ACTION_START_EVENT, runName );
      this.job = job;
      this.iterationNumber = iterationNumber;
      this.chaosAction = chaosAction;
   }

   @Builder
   public ChaosActionJobStartEvent( String runName, UUID id, Date timeOfOccurrence, TestJob job, int iterationNumber, ChaosAction chaosAction )
   {
      super( StandardEventsTypes.SCENARIO_CHAOS_ACTION_START_EVENT, runName, id, timeOfOccurrence );
      this.job = job;
      this.iterationNumber = iterationNumber;
      this.chaosAction = chaosAction;
   }
}
