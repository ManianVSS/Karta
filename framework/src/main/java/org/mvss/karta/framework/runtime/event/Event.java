package org.mvss.karta.framework.runtime.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class Event implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   protected String          eventType;
   protected String          runName;
   protected UUID            id;
   protected Date            timeOfOccurrence;

   public Event( String eventType, String runName )
   {
      this.eventType = eventType;
      this.id = UUID.randomUUID();
      this.runName = runName;
      this.timeOfOccurrence = Date.from( Instant.now() );
   }

   public Event( String runName )
   {
      this( StandardEventsTypes.UNDEFINED, runName );
   }
}
