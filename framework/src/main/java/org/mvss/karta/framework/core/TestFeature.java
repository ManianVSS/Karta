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
 * The test feature object. Kriya directly deserializes this object from Yaml files.
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
   private static final long                        serialVersionUID      = 1L;

   private String                                   name;
   private String                                   description;

   @Builder.Default
   private ArrayList<TestJob>                       testJobs              = new ArrayList<TestJob>();

   @Builder.Default
   private ArrayList<TestStep>                      setupSteps            = new ArrayList<TestStep>();

   @Builder.Default
   private ArrayList<TestStep>                      scenarioSetupSteps    = new ArrayList<TestStep>();

   @Builder.Default
   private ArrayList<TestScenario>                  testScenarios         = new ArrayList<TestScenario>();

   @Builder.Default
   private ArrayList<TestStep>                      scenarioTearDownSteps = new ArrayList<TestStep>();

   @Builder.Default
   private ArrayList<TestStep>                      tearDownSteps         = new ArrayList<TestStep>();

   private HashMap<String, ArrayList<Serializable>> testDataSet;
}
