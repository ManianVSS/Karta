package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureResult implements Serializable
{
   /**
    * 
    */
   private static final long                          serialVersionUID   = 1L;

   @Builder.Default
   private Date                                       startTime          = new Date();

   private Date                                       endTime;

   @Builder.Default
   private boolean                                    successful         = true;

   @Builder.Default
   private boolean                                    error              = false;

   @Builder.Default
   private CopyOnWriteArrayList<TestIncident>         incidents          = new CopyOnWriteArrayList<TestIncident>();

   @Builder.Default
   private HashMap<String, Boolean>                   setupResultMap     = new HashMap<String, Boolean>();

   @Builder.Default
   private HashMap<String, ArrayList<TestJobResult>>  jobsResultsMap     = new HashMap<String, ArrayList<TestJobResult>>();

   @Builder.Default
   private HashMap<String, ArrayList<ScenarioResult>> scenarioResultsMap = new HashMap<String, ArrayList<ScenarioResult>>();

   @Builder.Default
   private HashMap<String, Boolean>                   tearDownResultMap  = new HashMap<String, Boolean>();

   public HashMap<String, ArrayList<TestJobResult>> addTestJobResult( TestJob testJob, TestJobResult testJobResult )
   {
      if ( jobsResultsMap == null )
      {
         jobsResultsMap = new HashMap<String, ArrayList<TestJobResult>>();
      }

      if ( ( testJobResult == null ) && ( testJob != null ) )
      {
         String jobName = testJob.getName();
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

   public void addTestIterationResults( HashMap<TestScenario, ScenarioResult> iterationResults )
   {
      if ( scenarioResultsMap == null )
      {
         scenarioResultsMap = new HashMap<String, ArrayList<ScenarioResult>>();
      }

      if ( iterationResults == null )
      {
         return;
      }

      for ( Entry<TestScenario, ScenarioResult> entry : iterationResults.entrySet() )
      {
         TestScenario testScenario = entry.getKey();
         ScenarioResult scenarioResult = entry.getValue();

         successful = successful && scenarioResult.isPassed();
         ArrayList<ScenarioResult> scenarioResults = scenarioResultsMap.get( testScenario.getName() );

         if ( scenarioResults == null )
         {
            scenarioResults = new ArrayList<ScenarioResult>();
            scenarioResultsMap.put( testScenario.getName(), scenarioResults );
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
