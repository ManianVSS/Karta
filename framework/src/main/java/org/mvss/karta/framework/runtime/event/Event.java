package org.mvss.karta.framework.runtime.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event implements Serializable
{
   private static final long               serialVersionUID = 1L;

   protected String                        eventType;
   protected String                        runName;
   protected UUID                          id;
   protected Date                          timeOfOccurrence;
   protected HashMap<String, Serializable> parameters;

   public Event( String eventType, String runName )
   {
      this.eventType = eventType;
      this.id = UUID.randomUUID();
      this.runName = runName;
      this.timeOfOccurrence = Date.from( Instant.now() );
      this.parameters = new HashMap<String, Serializable>();
   }

   public Event( String runName )
   {
      this( StandardEventsTypes.UNDEFINED, runName );
   }

   public Event( String eventType, String runName, UUID id, Date timeOfOccurrence )
   {
      super();
      this.eventType = eventType;
      this.runName = runName;
      this.id = id;
      this.timeOfOccurrence = timeOfOccurrence;
      this.parameters = new HashMap<String, Serializable>();
   }

   public Event( Event event )
   {
      this.eventType = event.eventType;
      this.runName = event.runName;
      this.id = event.id;
      this.timeOfOccurrence = event.timeOfOccurrence;
      this.parameters = event.getParameters();
   }
}
