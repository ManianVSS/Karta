package org.mvss.karta.framework.core;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class describes a test feature object. Kriya directly deserializes this object from YAML files.
 *
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder( toBuilder = true )
@NoArgsConstructor
@AllArgsConstructor
public class TestFeature implements Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    * The name of the test feature.
    */
   private String name;

   /**
    * The description of the test feature.
    */
   private String description;

   /**
    * The list of jobs to run in background while running the scenarios.
    */
   @Builder.Default
   private ArrayList<TestJob> testJobs = new ArrayList<>();

   /**
    * The list of setup steps for the feature. To be run once for the feature.
    */
   @Builder.Default
   private ArrayList<TestStep> setupSteps = new ArrayList<>();

   /**
    * The list of common setup steps for all the scenarios of the feature. To be run before every scenario run.
    */
   @Builder.Default
   private ArrayList<TestStep> scenarioSetupSteps = new ArrayList<>();

   /**
    * The list of test scenarios for the feature.
    */
   @Builder.Default
   private ArrayList<TestScenario> testScenarios = new ArrayList<>();

   /**
    * The list of the common tear-down steps for all the scenarios of the feature. To be run after every scenario run.
    */
   @Builder.Default
   private ArrayList<TestStep> scenarioTearDownSteps = new ArrayList<>();

   /**
    * The list of tear-down steps for the feature. To be run once for the feature after completing all scenario executions.
    */
   @Builder.Default
   private ArrayList<TestStep> tearDownSteps = new ArrayList<>();

   /**
    * The possible set of values for test data at feature level.
    */
   private HashMap<String, ArrayList<Serializable>> testDataSet;
}
