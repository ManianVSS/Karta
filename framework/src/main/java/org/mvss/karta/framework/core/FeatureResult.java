package org.mvss.karta.framework.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

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
   private static final long                              serialVersionUID   = 1L;

   private String                                         featureName;

   @Builder.Default
   private Date                                           startTime          = new Date();

   private Date                                           endTime;

   @Builder.Default
   private boolean                                        successful         = true;

   @Builder.Default
   private boolean                                        error              = false;

   @Builder.Default
   private HashSet<TestIncident> incidents = new HashSet<>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, StepResult>> setupResults = new ArrayList<>();

   @Builder.Default
   private HashMap<String, ArrayList<ScenarioResult>> scenarioResultsMap = new HashMap<>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, StepResult>> tearDownResults = new ArrayList<>();

   @Builder.Default
   private ArrayList<Integer> failedIterations = new ArrayList<>();

   @JsonIgnore
   public boolean isPassed()
   {
      return successful && !error && incidents.isEmpty();
   }

   public synchronized void addIterationResult( HashMap<String, ScenarioResult> iterationResults, boolean detailedResults )
   {
      for ( Entry<String, ScenarioResult> entry : iterationResults.entrySet() )
      {
         addTestScenarioResult( entry.getKey(), entry.getValue(), detailedResults );
      }
   }

   public synchronized void addTestScenarioResult( String testScenario, ScenarioResult scenarioResult, boolean detailedResults )
   {
      if ( scenarioResult != null )
      {
         if ( scenarioResultsMap == null )
         {
            scenarioResultsMap = new HashMap<>();
         }

         boolean scenarioPassed = scenarioResult.isPassed();
         successful = successful && scenarioPassed;

         if ( detailedResults )
         {
            ArrayList<ScenarioResult> scenarioResults = scenarioResultsMap.computeIfAbsent( testScenario, k -> new ArrayList<>() );
            scenarioResults.add( scenarioResult );
         }
         if ( !scenarioPassed )
         {
            if ( failedIterations == null )
            {
               failedIterations = new ArrayList<>();
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

      for ( SerializableKVP<String, StepResult> tearDownResult : tearDownResults )
      {
         tearDownResult.getValue().processRemoteResults();
      }
   }
}
