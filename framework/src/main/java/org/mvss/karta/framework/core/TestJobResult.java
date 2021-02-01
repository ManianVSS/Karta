package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * The result of a test-job iteration
 * 
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestJobResult implements Serializable, Comparable<TestJobResult>
{
   private static final long                              serialVersionUID = 1L;

   @Builder.Default
   private long                                           iterationIndex   = 0;

   @Builder.Default
   private Date                                           startTime        = new Date();

   private Date                                           endTime;

   @Builder.Default
   private boolean                                        successful       = true;

   @Builder.Default
   private boolean                                        error            = false;

   @Builder.Default
   private ArrayList<SerializableKVP<String, StepResult>> stepResults      = new ArrayList<SerializableKVP<String, StepResult>>();

   @Override
   public int compareTo( TestJobResult other )
   {
      return ( iterationIndex < other.iterationIndex ) ? -1 : ( ( iterationIndex == other.iterationIndex ) ? 0 : 1 );
   }

   @JsonIgnore
   public boolean isPassed()
   {
      return successful && !error;
   }

   /**
    * Converts events and other objects received from remote execution to appropriate sub class
    */
   public void processRemoteResults()
   {
      for ( SerializableKVP<String, StepResult> setupResult : stepResults )
      {
         setupResult.getValue().processRemoteResults();
      }
   }
}
