package org.mvss.karta.framework.runtime.event;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

   public GenericTestEvent( String runName, Serializable eventData )
   {
      super( StandardEventsTypes.GENERIC_TEST_EVENT, runName );
      this.parameters.put( "eventData", eventData );
   }

   @JsonIgnore
   public Serializable getEventData()
   {
      return parameters.get( "eventData" );
   }
}
