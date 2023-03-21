package org.mvss.karta.samples.tests;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.dependencyinjection.annotations.PropertyMapping;
import org.mvss.karta.framework.annotations.javatest.*;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.models.run.TestExecutionContext;
import org.mvss.karta.samples.stepdefinitions.SamplePropertyType;

@Log4j2
@Feature("Sample Java Test Feature")
public class Test1 {
    @PropertyMapping(group = "groupName", value = "variable1")
    private String username = "default";

    @PropertyMapping(group = "groupName", value = "variable1")
    private String variable1;

    @PropertyMapping(group = "groupName", value = "variable2")
    private SamplePropertyType variable2;

    @FeatureSetup("My feature setup")
    public StepResult myFeatureSetup(TestExecutionContext testExecutionContext) {
        log.info("testData " + testExecutionContext.getData());
        return StandardStepResults.passed();
    }

    @ScenarioSetup("My scenario setup")
    public StepResult myScenarioSetupMethod(TestExecutionContext testExecutionContext) {
        log.info("testData " + testExecutionContext.getData());
        return StandardStepResults.passed();
    }

    @Scenario(value = "Scenario2", sequence = 2, probability = 0.33f)
    public StepResult myScenarioMethod2(TestExecutionContext testExecutionContext) {
        log.info(username + " " + variable2);
        return StandardStepResults.passed();
    }

    @Scenario(value = "Scenario1", sequence = 1, probability = 0.33f)
    public StepResult myScenarioMethod(TestExecutionContext testExecutionContext) {
        log.info(username + " " + variable1);
        return StandardStepResults.passed();
    }

    @Scenario(value = "Scenario3", probability = 0.34f)
    public StepResult myScenarioMethod3(TestExecutionContext context) {
        log.info(username + " " + variable2);
        // HashSet<String> failureTags = new HashSet<String>();
        // failureTags.add( "sample" );
        // failureTags.add( "failure" );
        // failureTags.add( "java" );
        // failureTags.add( "tags" );
        // StepResult result = new StepResult();
        // result.getIncidents().add( TestIncident.builder().message( "Sample test incident" ).tags( failureTags ).build() );
        // return result;
        return StandardStepResults.passed();
    }

    @ScenarioTearDown("My scenario teardown")
    public StepResult myScenarioTearDownMethod(TestExecutionContext testExecutionContext) {
        log.info("testData " + testExecutionContext.getData());
        return StandardStepResults.passed();
    }

    @FeatureTearDown("My feature teardown")
    public StepResult myFeatureTearDownMethod(TestExecutionContext testExecutionContext) {
        log.info("testData " + testExecutionContext.getData());
        return StandardStepResults.passed();
    }
}
