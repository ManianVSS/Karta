package org.mvss.karta.framework.core;

import org.mvss.karta.framework.chaos.ChaosActionTreeNode;
import org.mvss.karta.framework.randomization.ObjectWithChance;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class describes a test scenario object.
 *
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder( toBuilder = true )
@AllArgsConstructor
@NoArgsConstructor
public class TestScenario implements Serializable, ObjectWithChance
{
   private static final long serialVersionUID = 1L;

   /**
    * The name of the test scenario.
    */
   private String name;

   /**
    * The description of the test scenario.
    */
   private String description;

   /**
    * The probability that this test scenario should be run for an iteration of the feature run.
    * This value is used when the run configuration for the feature is to run exclusive scenario per iteration or probability based selection of scenarios for feature run iteration.
    */
   @Builder.Default
   private float probability = 1.0f;

   /**
    * Setup steps for the scenario run before execution steps and chaos action.
    */
   @Builder.Default
   private ArrayList<TestStep> setupSteps = new ArrayList<>();

   /**
    * Chaos configuration for the scenario. If not null, chaos actions generated from the configuration are run after scenario setup and before execution steps.
    */
   private ChaosActionTreeNode chaosConfiguration;

   /**
    * The execution steps for the test scenario.
    */
   @Builder.Default
   private ArrayList<TestStep> executionSteps = new ArrayList<>();

   /**
    * The tear down steps for the test scenario to be run after execution step(pass or fail).
    */
   @Builder.Default
   private ArrayList<TestStep> tearDownSteps = new ArrayList<>();

   /**
    * The possible set of values for test data at scenario level.
    */
   private HashMap<String, ArrayList<Serializable>> testDataSet;
}
