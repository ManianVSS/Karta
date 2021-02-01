package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.event.StandardEventsTypes;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This class captures the execution results of a prepared scenario
 * 
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder( toBuilder = true )
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioResult implements Serializable, Comparable<ScenarioResult>
{
   private static final long                              serialVersionUID   = 1L;

   @Builder.Default
   private long                                           iterationIndex     = 0;

   @Builder.Default
   private Date                                           startTime          = new Date();

   private Date                                           endTime;

   @Builder.Default
   private boolean                                        successful         = true;

   @Builder.Default
   private boolean                                        error              = false;

   @Builder.Default
   private ArrayList<TestIncident>                        incidents          = new ArrayList<TestIncident>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, StepResult>> setupResults       = new ArrayList<SerializableKVP<String, StepResult>>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, StepResult>> chaosActionResults = new ArrayList<SerializableKVP<String, StepResult>>();

   // TODO: Add more details to step results: start time and end time
   @Builder.Default
   private ArrayList<SerializableKVP<String, StepResult>> runResults         = new ArrayList<SerializableKVP<String, StepResult>>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, StepResult>> tearDownResults    = new ArrayList<SerializableKVP<String, StepResult>>();

   @Builder.Default
   private ArrayList<Event>                               events             = new ArrayList<Event>();

   @JsonIgnore
   public boolean isPassed()
   {
      return successful && !error && incidents.isEmpty();
   }

   @Override
   public int compareTo( ScenarioResult other )
   {
      return (int) ( ( iterationIndex - other.iterationIndex ) % Integer.MAX_VALUE );
   }

   public ScenarioResult trimForReport()
   {
      ScenarioResult trimmedResult = ScenarioResult.builder().iterationIndex( iterationIndex ).startTime( startTime ).endTime( endTime ).successful( successful ).error( error ).events( null ).build();

      for ( SerializableKVP<String, StepResult> setupResult : setupResults )
      {
         trimmedResult.setupResults.add( new SerializableKVP<String, StepResult>( setupResult.getKey(), setupResult.getValue().trimForReport() ) );
      }
      for ( SerializableKVP<String, StepResult> chaosActionResult : chaosActionResults )
      {
         trimmedResult.chaosActionResults.add( new SerializableKVP<String, StepResult>( chaosActionResult.getKey(), chaosActionResult.getValue().trimForReport() ) );
      }
      for ( SerializableKVP<String, StepResult> runResult : runResults )
      {
         trimmedResult.runResults.add( new SerializableKVP<String, StepResult>( runResult.getKey(), runResult.getValue().trimForReport() ) );
      }
      for ( SerializableKVP<String, StepResult> tearDownResult : tearDownResults )
      {
         trimmedResult.tearDownResults.add( new SerializableKVP<String, StepResult>( tearDownResult.getKey(), tearDownResult.getValue().trimForReport() ) );
      }

      return trimmedResult;
   }

   /**
    * Converts events and other objects received from remote execution to appropriate sub class
    */
   public void processRemoteResults()
   {
      ArrayList<Event> newEvents = new ArrayList<Event>();
      events.forEach( ( event ) -> newEvents.add( StandardEventsTypes.castToAppropriateEvent( event ) ) );
      events.clear();
      events = newEvents;

      for ( SerializableKVP<String, StepResult> setupResult : setupResults )
      {
         setupResult.getValue().processRemoteResults();
      }
      for ( SerializableKVP<String, StepResult> chaosActionResult : chaosActionResults )
      {
         chaosActionResult.getValue().processRemoteResults();
      }
      for ( SerializableKVP<String, StepResult> runResult : runResults )
      {
         runResult.getValue().processRemoteResults();
      }
      for ( SerializableKVP<String, StepResult> tearDownResult : tearDownResults )
      {
         tearDownResult.getValue().processRemoteResults();
      }
   }
}
