package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;

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
   private CopyOnWriteArrayList<TestIncident>             incidents          = new CopyOnWriteArrayList<TestIncident>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, StepResult>> setupResults       = new ArrayList<SerializableKVP<String, StepResult>>();

   @Builder.Default
   private HashMap<String, ArrayList<TestJobResult>>      jobsResultsMap     = new HashMap<String, ArrayList<TestJobResult>>();

   @Builder.Default
   private HashMap<String, ArrayList<ScenarioResult>>     scenarioResultsMap = new HashMap<String, ArrayList<ScenarioResult>>();

   @Builder.Default
   private ArrayList<SerializableKVP<String, StepResult>> tearDownResults    = new ArrayList<SerializableKVP<String, StepResult>>();

   public HashMap<String, ArrayList<TestJobResult>> addTestJobResult( String jobName, TestJobResult testJobResult )
   {
      if ( jobsResultsMap == null )
      {
         jobsResultsMap = new HashMap<String, ArrayList<TestJobResult>>();
      }

      if ( ( testJobResult != null ) && StringUtils.isNotBlank( jobName ) )
      {
         ArrayList<TestJobResult> jobResults = jobsResultsMap.get( jobName );

         if ( jobResults == null )
         {
            jobResults = new ArrayList<TestJobResult>();
            jobsResultsMap.put( jobName, jobResults );
         }

         jobResults.add( testJobResult );
      }
      return jobsResultsMap;
   }

   @JsonIgnore
   public boolean isPassed()
   {
      return successful && !error && incidents.isEmpty();
   }

   public void addIterationResult( HashMap<String, ScenarioResult> iterationResults )
   {
      for ( Entry<String, ScenarioResult> entry : iterationResults.entrySet() )
      {
         addTestScenarioResult( entry.getKey(), entry.getValue() );
      }
   }

   public void addTestScenarioResult( String testScenario, ScenarioResult scenarioResult )
   {
      if ( scenarioResultsMap == null )
      {
         scenarioResultsMap = new HashMap<String, ArrayList<ScenarioResult>>();
      }

      if ( scenarioResult != null )
      {
         successful = successful && scenarioResult.isPassed();

         ArrayList<ScenarioResult> scenarioResults = scenarioResultsMap.get( testScenario );

         if ( scenarioResults == null )
         {
            scenarioResults = new ArrayList<ScenarioResult>();
            scenarioResultsMap.put( testScenario, scenarioResults );
         }
         scenarioResults.add( scenarioResult );
      }
   }

   public synchronized void sortResults()
   {
      jobsResultsMap.values().forEach( ( results ) -> Collections.sort( results ) );
      scenarioResultsMap.values().forEach( ( results ) -> Collections.sort( results ) );
   }
}
