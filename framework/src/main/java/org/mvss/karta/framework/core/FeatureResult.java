package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

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
   private static final long                                serialVersionUID   = 1L;

   @Builder.Default
   private Date                                             startTime          = new Date();

   private Date                                             endTime;

   @Builder.Default
   private boolean                                          successsful        = true;

   @Builder.Default
   private boolean                                          error              = false;

   @Builder.Default
   private ArrayList<TestIncident>                          incidents          = new ArrayList<TestIncident>();

   @Builder.Default
   private HashMap<TestStep, StepResult>                    setupResultMap     = new HashMap<TestStep, StepResult>();

   @Builder.Default
   private HashMap<TestJob, ArrayList<TestJobResult>>       jobsResultsMap     = new HashMap<TestJob, ArrayList<TestJobResult>>();

   @Builder.Default
   private HashMap<TestScenario, ArrayList<ScenarioResult>> scenarioResultsMap = new HashMap<TestScenario, ArrayList<ScenarioResult>>();

   @Builder.Default
   private HashMap<TestStep, StepResult>                    tearDownResultMap  = new HashMap<TestStep, StepResult>();

   public void addTestJobResult( TestJob testJob, TestJobResult testJobResult )
   {
      if ( jobsResultsMap == null )
      {
         jobsResultsMap = new HashMap<TestJob, ArrayList<TestJobResult>>();
      }

      if ( testJobResult == null )
      {
         ArrayList<TestJobResult> jobResults = jobsResultsMap.get( testJob );

         if ( jobResults == null )
         {
            jobResults = new ArrayList<TestJobResult>();
            jobsResultsMap.put( testJob, jobResults );
         }

         jobResults.add( testJobResult );
      }
   }

   public void addIterationResult( HashMap<TestScenario, ScenarioResult> iterationResults )
   {
      for ( Entry<TestScenario, ScenarioResult> entry : iterationResults.entrySet() )
      {
         addTestScenarioResult( entry.getKey(), entry.getValue() );
      }
   }

   public void addTestScenarioResult( TestScenario testScenario, ScenarioResult scenarioResult )
   {
      if ( scenarioResultsMap == null )
      {
         scenarioResultsMap = new HashMap<TestScenario, ArrayList<ScenarioResult>>();
      }

      if ( scenarioResult != null )
      {
         ArrayList<ScenarioResult> scenarioResults = scenarioResultsMap.get( testScenario );

         if ( scenarioResults == null )
         {
            scenarioResults = new ArrayList<ScenarioResult>();
            scenarioResultsMap.put( testScenario, scenarioResults );
         }
         scenarioResults.add( scenarioResult );
      }
   }
}
