package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This class describes a test step object.
 * 
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder( toBuilder = true )
@NoArgsConstructor
@AllArgsConstructor
public class TestStep implements Serializable
{
   private static final long                              serialVersionUID   = 1L;

   /**
    * The step identifier which is used to map the step in feature files to step definitions of step runner.
    */
   private String                                         step;

   /**
    * The test data passed along with the step.
    */
   private HashMap<String, Serializable>                  testData;

   /**
    * The possible test data values for the test step.
    */
   private HashMap<String, ArrayList<Serializable>>       testDataSet;

   /**
    * Object generation rule mapped by variable name to rule for generating test data.
    * 
    * @see org.mvss.karta.framework.randomization.ObjectGenerationRule
    */
   private HashMap<String, HashMap<String, Serializable>> variableTestDataRules;

   /**
    * The node on which the step should be run. The node name is not a hostname/ip but a role say "InventoryServer" which is mapped to a host in Karta configuration.
    */
   private String                                         node;

   /**
    * Indicates if the same step is to be run in multiple threads in parallel.
    */
   @Builder.Default
   private Integer                                        numberOfThreads    = 1;

   /**
    * The number of times this step should be retried if failed
    */
   @Builder.Default
   private Integer                                        maxRetries         = 0;

   /**
    * The group of steps to run.
    */
   private ArrayList<TestStep>                            steps;

   /**
    * Indicates if this is a group of steps to be run in parallel or in sequence.
    */
   @Builder.Default
   private Boolean                                        runStepsInParallel = false;

}
