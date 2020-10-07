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
public class RunStartEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public RunStartEvent( String runName )
   {
      super( StandardEventsTypes.RUN_START_EVENT, runName );
   }

   @Builder
   public RunStartEvent( String runName, UUID id, Date timeOfOccurrence )
   {
      super( StandardEventsTypes.RUN_START_EVENT, runName, id, timeOfOccurrence );
   }
}
