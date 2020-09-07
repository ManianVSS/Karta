package org.mvss.karta.framework.core;

import java.util.ArrayList;

import org.mvss.karta.framework.enums.ScenarioRunPolicy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TestFeature
{
   private String                  testCaseReference;
   private String                  description;

   private long                    numberOfIterations;
   private ScenarioRunPolicy       scenarioRunPolicy           = ScenarioRunPolicy.RUN_ALL_EVERY_ITERATION;

   private ArrayList<TestStep>     testSetupSteps              = new ArrayList<TestStep>();
   private ArrayList<TestStep>     commonScenarioSetupSteps    = new ArrayList<TestStep>();
   private ArrayList<TestScenario> testScenarios               = new ArrayList<TestScenario>();
   private ArrayList<TestStep>     commonScenarioTearDownSteps = new ArrayList<TestStep>();
   private ArrayList<TestStep>     testTearDownSteps           = new ArrayList<TestStep>();
}
