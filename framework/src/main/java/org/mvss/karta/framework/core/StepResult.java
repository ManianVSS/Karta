package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.mvss.karta.framework.runtime.event.Event;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
   private static final long             serialVersionUID = 1L;

   /**
    * Start time of step execution.
    */
   @Builder.Default
   private Date                          startTime        = new Date();

   /**
    * End time of step execution.
    */
   @Builder.Default
   private Date                          endTime          = null;

   /**
    * Indicates whether the step was successful
    */
   @Builder.Default
   private boolean                       successful       = true;

   /**
    * Indicates whether the step has an unexpected error/exception indicate test script failure
    */
   @Builder.Default
   private boolean                       error            = false;

   /**
    * List of the test incidents observed during the step execution.
    * This can be used to associate additional failures for step execution which don't necessarily fail the step's.
    */
   @Builder.Default
   private ArrayList<TestIncident>       incidents        = new ArrayList<TestIncident>();

   /**
    * Mapping of return result variable names to serializable values of the step
    */
   @Builder.Default
   private HashMap<String, Serializable> results          = new HashMap<String, Serializable>();

   /**
    * List of events to be raised after step execution. Used for raising custom events from steps.
    */
   @Builder.Default
   private ArrayList<Event>              events           = new ArrayList<Event>();

   /**
    * Map of attachment names to attachment data. Can be used store data like screenshots.
    */
   @Builder.Default
   private HashMap<String, Serializable> attachments      = new HashMap<String, Serializable>();

   /**
    * The index of this step. Used to distinguish multiple use of same steps in scenario.
    */
   private long                          stepIndex;

   /**
    * Indicates if the test passed
    * 
    * @return boolean
    */
   @JsonIgnore
   public boolean isPassed()
   {
      return successful && !error && incidents.isEmpty();
   }

   /**
    * Add a test incident to the step result
    * 
    * @param testIncident
    */
   public void addIncident( TestIncident testIncident )
   {
      if ( incidents == null )
      {
         incidents = new ArrayList<TestIncident>();
      }
      incidents.add( testIncident );
   }

   /**
    * Merge the step result into current result.
    * 
    * @param stepResult
    */
   public void merge( StepResult stepResult )
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
      error = error && stepResult.error;

      if ( stepResult.incidents != null )
      {
         if ( incidents == null )
         {
            incidents = new ArrayList<TestIncident>();
         }
         incidents.addAll( stepResult.incidents );
      }

      if ( stepResult.results != null )
      {
         if ( results == null )
         {
            results = new HashMap<String, Serializable>();
         }
         this.results.putAll( stepResult.results );
      }

      if ( stepResult.events != null )
      {
         if ( events == null )
         {
            events = new ArrayList<Event>();
         }
         events.addAll( stepResult.events );
      }

   }

   /**
    * Get a trimmed version which don't contain return result variables or events or attachments
    * 
    * @return
    */
   public StepResult trimForReport()
   {
      return this.toBuilder().results( null ).events( null ).attachments( null ).build();
   }
}
