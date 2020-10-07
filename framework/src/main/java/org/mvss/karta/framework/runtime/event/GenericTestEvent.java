package org.mvss.karta.framework.runtime.event;

import java.io.Serializable;
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
public class GenericTestEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private Serializable      eventData;

   public GenericTestEvent( String runName, Serializable eventData )
   {
      super( StandardEventsTypes.GENERIC_TEST_EVENT, runName );
      this.eventData = eventData;
   }

   @Builder
   public GenericTestEvent( String runName, UUID id, Date timeOfOccurrence, Serializable eventData )
   {
      super( StandardEventsTypes.GENERIC_TEST_EVENT, runName, id, timeOfOccurrence );
      this.eventData = eventData;
   }
}
