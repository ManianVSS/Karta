package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import org.mvss.karta.framework.runtime.event.Event;

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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScenarioResult implements Serializable, Comparable<ScenarioResult>
{
   private static final long                           serialVersionUID   = 1L;

   @Builder.Default
   private long                                        iterationIndex     = 0;

   @Builder.Default
   private Date                                        startTime          = new Date();

   private Date                                        endTime;

   @Builder.Default
   private boolean                                     successful         = true;

   @Builder.Default
   private boolean                                     error              = false;

   @Builder.Default
   private ArrayList<TestIncident>                     incidents          = new ArrayList<TestIncident>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, Boolean>> setupResults       = new ArrayList<SerializableKVP<String, Boolean>>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, Boolean>> chaosActionResults = new ArrayList<SerializableKVP<String, Boolean>>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, Boolean>> runResults         = new ArrayList<SerializableKVP<String, Boolean>>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, Boolean>> tearDownResults    = new ArrayList<SerializableKVP<String, Boolean>>();

   @Builder.Default
   private ArrayList<Event>                            events             = new ArrayList<Event>();

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
}
