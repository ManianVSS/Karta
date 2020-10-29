package org.mvss.karta.framework.runtime.reports;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureReport implements Serializable
{
   /**
    * 
    */
   private static final long                              serialVersionUID = 1L;

   @Builder.Default
   private HashMap<TestStep, StepResult>                  setupReport      = new HashMap<TestStep, StepResult>();

   @Builder.Default
   private HashMap<TestJob, HashMap<Integer, StepResult>> jobsReport       = new HashMap<TestJob, HashMap<Integer, StepResult>>();

   @Builder.Default
   private HashMap<TestScenario, ScenarioReport>          iterationsReport = new HashMap<TestScenario, ScenarioReport>();

   @Builder.Default
   private HashMap<TestStep, StepResult>                  tearDownReport   = new HashMap<TestStep, StepResult>();

   public void addOrCreateTestJobReport( TestJob testJob, Integer iterationIndex, StepResult result )
   {
      if ( jobsReport == null )
      {
         jobsReport = new HashMap<TestJob, HashMap<Integer, StepResult>>();
      }

      HashMap<Integer, StepResult> iterationJobReport = jobsReport.get( testJob );

      if ( iterationJobReport == null )
      {
         jobsReport.put( testJob, iterationJobReport = new HashMap<Integer, StepResult>() );
      }

      iterationJobReport.get( iterationIndex );

      if ( iterationJobReport.containsKey( iterationIndex ) )
      {

      }
   }

   public ScenarioReport getOrCreateScenarioReport( TestScenario scenario, Integer iterationIndex )
   {
      if ( iterationsReport == null )
      {
         iterationsReport = new HashMap<TestScenario, ScenarioReport>();
      }
      return null;
      // iterationsReport.get
   }
}
