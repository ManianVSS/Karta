package org.mvss.karta.framework.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.event.StandardEventsTypes;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * The execution results for a step.
 *
 * @author Manian
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder( toBuilder = true )
public class StepResult implements Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    * Start time of step execution.
    */
   @Builder.Default
   private Date startTime = new Date();

   /**
    * End time of step execution.
    */
   @Builder.Default
   private Date endTime = null;

   /**
    * Indicates whether the step was successful
    */
   @Builder.Default
   private boolean successful = true;

   /**
    * Indicates whether the step has an unexpected error/exception indicate test script failure
    */
   @Builder.Default
   private boolean error = false;

   /**
    * List of the test incidents observed during the step execution.
    * This can be used to associate additional failures for step execution which don't necessarily fail the step's.
    */
   @Builder.Default
   private ArrayList<TestIncident> incidents = new ArrayList<>();

   /**
    * Mapping of return result variable names to serializable values of the step
    */
   @Builder.Default
   private HashMap<String, Serializable> results = new HashMap<>();

   /**
    * List of events to be raised after step execution. Used for raising custom events from steps.
    */
   @Builder.Default
   private ArrayList<Event> events = new ArrayList<>();

   /**
    * Map of attachment names to attachment data. Can be used store data like screenshots.
    */
   @Builder.Default
   private HashMap<String, Serializable> attachments = new HashMap<>();

   /**
    * The index of this step. Used to distinguish multiple use of same steps in scenario.
    */
   private long stepIndex;

   /**
    * Indicates if the test passed
    *
    * @return boolean
    */
   @JsonIgnore
   public boolean isPassed()
   {
      return successful && !error;
   }

   /**
    * Indicates if the test failed and does not have error
    *
    * @return boolean
    */
   @JsonIgnore
   public boolean isFailed()
   {
      return !error && !( successful && incidents.isEmpty() );
   }

   /**
    * Add a test incident to the step result
    *
    * @param testIncident TestIncident
    */
   public void addIncident( TestIncident testIncident )
   {
      if ( incidents == null )
      {
         incidents = new ArrayList<>();
      }
      incidents.add( testIncident );
   }

   /**
    * Merges the results from a step result into this.
    *
    * @param stepResult StepResult
    */
   public synchronized void mergeResults( StepResult stepResult )
   {
      if ( stepResult == null )
      {
         return;
      }

      if ( stepResult.startTime != null )
      {
         this.startTime = stepResult.startTime;
      }

      if ( stepResult.endTime != null )
      {
         this.endTime = stepResult.endTime;
      }

      successful = successful && stepResult.successful;
      error      = error || stepResult.error;

      if ( stepResult.incidents != null )
      {
         if ( incidents == null )
         {
            incidents = new ArrayList<>();
         }
         incidents.addAll( stepResult.incidents );
      }

      if ( stepResult.results != null )
      {
         if ( results == null )
         {
            results = new HashMap<>();
         }
         this.results.putAll( stepResult.results );
      }

      if ( stepResult.events != null )
      {
         if ( events == null )
         {
            events = new ArrayList<>();
         }
         events.addAll( stepResult.events );
      }

      if ( stepResult.attachments != null )
      {
         if ( attachments == null )
         {
            attachments = new HashMap<>();
         }
         attachments.putAll( stepResult.attachments );
      }
   }

   /**
    * Get a trimmed version which don't contain return result variables or events or attachments
    *
    * @return StepResult
    */
   public StepResult trimForReport()
   {
      return this.toBuilder().results( null ).events( null ).attachments( null ).build();
   }

   /**
    * Convert events and other objects received from remote execution to appropriate subclass
    */
   public void processRemoteResults()
   {
      ArrayList<Event> newEvents = new ArrayList<>();
      events.forEach( ( event ) -> newEvents.add( StandardEventsTypes.castToAppropriateEvent( event ) ) );
      events.clear();
      events = newEvents;
   }
}
