package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestFeature implements Serializable
{
   /**
    * 
    */
   private static final long       serialVersionUID              = 1L;

   private String                  name;
   private String                  description;

   @Builder.Default
   private Boolean                 chanceBasedScenarioExecution  = false;

   @Builder.Default
   private Boolean                 exclusiveScenarioPerIteration = false;

   @Builder.Default
   private ArrayList<TestJob>      testJobs                      = new ArrayList<TestJob>();

   @Builder.Default
   private ArrayList<TestStep>     setupSteps                    = new ArrayList<TestStep>();

   @Builder.Default
   private ArrayList<TestStep>     scenarioSetupSteps            = new ArrayList<TestStep>();

   @Builder.Default
   private ArrayList<TestScenario> testScenarios                 = new ArrayList<TestScenario>();

   @Builder.Default
   private ArrayList<TestStep>     scenarioTearDownSteps         = new ArrayList<TestStep>();

   @Builder.Default
   private ArrayList<TestStep>     tearDownSteps                 = new ArrayList<TestStep>();
}
