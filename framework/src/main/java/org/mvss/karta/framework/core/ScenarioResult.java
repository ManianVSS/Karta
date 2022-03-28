package org.mvss.karta.framework.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.event.StandardEventsTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

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
   private static final long serialVersionUID = 1L;

   @Builder.Default
   private int iterationIndex = 0;

   @Builder.Default
   private Date startTime = new Date();

   private Date endTime;

   @Builder.Default
   private boolean successful = true;

   @Builder.Default
   private boolean error = false;

   @Builder.Default
   private ArrayList<TestIncident> incidents = new ArrayList<>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, StepResult>> setupResults = new ArrayList<>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, StepResult>> chaosActionResults = new ArrayList<>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, StepResult>> runResults = new ArrayList<>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, StepResult>> tearDownResults = new ArrayList<>();

   @Builder.Default
   private ArrayList<Event> events = new ArrayList<>();

   @JsonIgnore
   public boolean isPassed()
   {
      return successful && !error;
   }

   @Override
   public int compareTo( ScenarioResult other )
   {
      return ( iterationIndex - other.iterationIndex ) % Integer.MAX_VALUE;
   }

   public ScenarioResult trimForReport()
   {
      ScenarioResult trimmedResult = ScenarioResult.builder().iterationIndex( iterationIndex ).startTime( startTime ).endTime( endTime )
               .successful( successful ).error( error ).events( null ).build();

      for ( SerializableKVP<String, StepResult> setupResult : setupResults )
      {
         trimmedResult.setupResults.add( new SerializableKVP<>( setupResult.getKey(), setupResult.getValue().trimForReport() ) );
      }
      for ( SerializableKVP<String, StepResult> chaosActionResult : chaosActionResults )
      {
         trimmedResult.chaosActionResults.add( new SerializableKVP<>( chaosActionResult.getKey(), chaosActionResult.getValue().trimForReport() ) );
      }
      for ( SerializableKVP<String, StepResult> runResult : runResults )
      {
         trimmedResult.runResults.add( new SerializableKVP<>( runResult.getKey(), runResult.getValue().trimForReport() ) );
      }
      for ( SerializableKVP<String, StepResult> tearDownResult : tearDownResults )
      {
         trimmedResult.tearDownResults.add( new SerializableKVP<>( tearDownResult.getKey(), tearDownResult.getValue().trimForReport() ) );
      }

      return trimmedResult;
   }

   /**
    * Converts events and other objects received from remote execution to appropriate subclass
    */
   public void processRemoteResults()
   {
      ArrayList<Event> newEvents = new ArrayList<>();
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
