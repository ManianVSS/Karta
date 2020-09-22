package org.mvss.karta.framework.core;

import java.util.ArrayList;
import java.util.HashSet;

import org.mvss.karta.framework.enums.ScenarioRunPolicy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestFeature
{
   private String                  name;
   private String                  description;

   @Builder.Default
   private Long                    numberOfIterations    = 1l;

   @Builder.Default
   private HashSet<String>         tags                  = new HashSet<String>();

   @Builder.Default
   private ScenarioRunPolicy       scenarioRunPolicy     = ScenarioRunPolicy.RUN_ALL_EVERY_ITERATION;

   @Builder.Default
   private ArrayList<TestStep>     testSetupSteps        = new ArrayList<TestStep>();

   @Builder.Default
   private ArrayList<TestStep>     scenarioSetupSteps    = new ArrayList<TestStep>();

   @Builder.Default
   private ArrayList<TestScenario> testScenarios         = new ArrayList<TestScenario>();

   @Builder.Default
   private ArrayList<TestStep>     scenarioTearDownSteps = new ArrayList<TestStep>();

   @Builder.Default
   private ArrayList<TestStep>     testTearDownSteps     = new ArrayList<TestStep>();
}
