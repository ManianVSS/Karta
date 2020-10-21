package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.core.TestIncident;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class TestIncidentOccurenceEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private TestIncident      incident;

   public TestIncidentOccurenceEvent( String runName, TestIncident incident )
   {
      super( StandardEventsTypes.FEATURE_SETUP_STEP_COMPLETE_EVENT, runName );
      this.incident = incident;
   }

   @Builder
   public TestIncidentOccurenceEvent( String runName, UUID id, Date timeOfOccurrence, TestIncident incident )
   {
      super( StandardEventsTypes.FEATURE_SETUP_STEP_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.incident = incident;
   }
}
