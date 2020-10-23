package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class RunCompleteEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public RunCompleteEvent( String runName )
   {
      super( StandardEventsTypes.JOB_STEP_COMPLETE_EVENT, runName );
   }

   @Builder
   public RunCompleteEvent( String runName, UUID id, Date timeOfOccurrence )
   {
      super( StandardEventsTypes.JOB_STEP_COMPLETE_EVENT, runName, id, timeOfOccurrence );
   }
}
