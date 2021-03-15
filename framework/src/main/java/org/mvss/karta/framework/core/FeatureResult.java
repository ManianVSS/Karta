package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Stores results of a TestFeature execution.
 * 
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureResult implements Serializable
{
   private static final long                              serialVersionUID = 1L;

   private String                                         featureName;

   @Builder.Default
   private Date                                           startTime        = new Date();

   private Date                                           endTime;

   @Builder.Default
   private boolean                                        successful       = true;

   @Builder.Default
   private boolean                                        error            = false;

   @Builder.Default
   private HashSet<TestIncident>                          incidents        = new HashSet<TestIncident>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, StepResult>> setupResults     = new ArrayList<SerializableKVP<String, StepResult>>();

   @Builder.Default
   private ArrayList<Integer>                             failedIterations = new ArrayList<Integer>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, StepResult>> tearDownResults  = new ArrayList<SerializableKVP<String, StepResult>>();

   @JsonIgnore
   public boolean isPassed()
   {
      return successful && !error && incidents.isEmpty();
   }

   public synchronized void addIterationResult( HashMap<String, ScenarioResult> iterationResults )
   {
      for ( Entry<String, ScenarioResult> entry : iterationResults.entrySet() )
      {
         addTestScenarioResult( entry.getKey(), entry.getValue() );
      }
   }

   public synchronized void addTestScenarioResult( String testScenario, ScenarioResult scenarioResult )
   {
      if ( scenarioResult != null )
      {
         boolean scenarioPassed = scenarioResult.isPassed();
         successful = successful && scenarioPassed;

         if ( !scenarioPassed )
         {
            if ( failedIterations == null )
            {
               failedIterations = new ArrayList<Integer>();
            }

            int failedIteration = scenarioResult.getIterationIndex();
            if ( !failedIterations.contains( failedIteration ) )
            {
               failedIterations.add( failedIteration );
            }

            ArrayList<TestIncident> scenarioIncidents = scenarioResult.getIncidents();

            if ( scenarioIncidents != null )
            {
               incidents.addAll( scenarioIncidents );
            }
         }
      }
   }

   public synchronized void sortResults()
   {
      Collections.sort( failedIterations );
   }

   /**
    * Converts events and other objects received from remote execution to appropriate sub class
    */
   public void processRemoteResults()
   {

      for ( SerializableKVP<String, StepResult> setupResult : setupResults )
      {
         setupResult.getValue().processRemoteResults();
      }
      // for ( Entry<String, ArrayList<TestJobResult>> chaosActionResult : jobsResultsMap.entrySet() )
      // {
      // for ( TestJobResult testJobResult : chaosActionResult.getValue() )
      // {
      // testJobResult.processRemoteResults();
      // }
      // }
      // for ( Entry<String, ArrayList<ScenarioResult>> runResult : scenarioResultsMap.entrySet() )
      // {
      // for ( ScenarioResult scenarioResult : runResult.getValue() )
      // {
      // scenarioResult.processRemoteResults();
      // }
      // }
      for ( SerializableKVP<String, StepResult> tearDownResult : tearDownResults )
      {
         tearDownResult.getValue().processRemoteResults();
      }
   }
}
