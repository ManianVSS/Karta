package org.mvss.karta.server.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.core.StandardFeatureResults;
import org.mvss.karta.framework.core.StandardScenarioResults;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.models.result.FeatureResult;
import org.mvss.karta.framework.models.result.ScenarioResult;
import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.models.result.TestJobResult;
import org.mvss.karta.framework.models.run.RunInfo;
import org.mvss.karta.framework.models.test.*;
import org.mvss.karta.framework.nodes.dto.*;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.TestFailureException;
import org.mvss.karta.framework.runtime.TestJobRunner;
import org.mvss.karta.framework.utils.ParserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class MinionController {
    private final ObjectMapper objectMapper = ParserUtils.getObjectMapper();
    @Autowired
    private KartaRuntime kartaRuntime;

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET, value = Constants.PATH_HEALTH)
    public boolean healthCheck() {
        return true;
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST, value = Constants.PATH_RUN_STEP)
    public StepResult runStep(@RequestBody StepRunInfo stepRunInfo) {
        try {
            if (stepRunInfo == null) {
                return StandardStepResults.error("Missing StepRunInfo in body");
            }

            RunInfo runInfo = stepRunInfo.getRunInfo();
            if (runInfo == null) {
                runInfo = kartaRuntime.getDefaultRunInfo();
            }
            PreparedStep testStep = stepRunInfo.getPreparedStep();

            if (testStep == null) {
                return StandardStepResults.error("Step to run missing in StepRunInfo");
            }

            return kartaRuntime.runStep(runInfo, testStep);
        } catch (TestFailureException e) {
            return StandardStepResults.failure(e);
        } catch (Throwable t) {
            return StandardStepResults.error(t);
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST, value = Constants.PATH_RUN_CHAOS_ACTION)
    public StepResult runChaosAction(@RequestBody ChaosActionRunInfo chaosActionRunInfo) {
        try {
            if (chaosActionRunInfo == null) {
                return StandardStepResults.error("Missing ChaosActionRunInfo in body");
            }
            RunInfo runInfo = chaosActionRunInfo.getRunInfo();
            if (runInfo == null) {
                runInfo = kartaRuntime.getDefaultRunInfo();
            }
            PreparedChaosAction chaosAction = chaosActionRunInfo.getPreparedChaosAction();

            if (chaosAction == null) {
                return StandardStepResults.error("Chaos action to run missing in ChaosActionRunInfo");
            }

            return kartaRuntime.runChaosAction(runInfo, chaosAction);
        } catch (TestFailureException e) {
            return StandardStepResults.failure(e);
        } catch (Throwable t) {
            return StandardStepResults.error(t);
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST, value = Constants.PATH_RUN_SCENARIO)
    public ScenarioResult runScenario(@RequestBody ScenarioRunInfo scenarioRunInfo) {
        try {
            if (scenarioRunInfo == null) {
                return StandardScenarioResults.error("Missing ScenarioRunInfo in body");
            }
            RunInfo runInfo = scenarioRunInfo.getRunInfo();
            if (runInfo == null) {
                runInfo = kartaRuntime.getDefaultRunInfo();
            }
            String featureName = scenarioRunInfo.getFeatureName();
            int iterationIndex = scenarioRunInfo.getIterationIndex();
            PreparedScenario testScenario = scenarioRunInfo.getPreparedScenario();
            long scenarioIterationNumber = scenarioRunInfo.getScenarioIterationNumber();

            if (testScenario == null) {
                return StandardScenarioResults.error("Scenario to run missing in ScenarioRunInfo");
            }
            testScenario.normalizeVariables();
            return kartaRuntime.runTestScenario(runInfo, featureName, iterationIndex, testScenario, scenarioIterationNumber);
        } catch (Throwable t) {
            return StandardScenarioResults.error(t);
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST, value = Constants.PATH_RUN_JOB_ITERATION)
    public TestJobResult runJobIteration(@RequestBody JobIterationRunInfo jobIterationRunInfo) throws Throwable {
        if (jobIterationRunInfo == null) {
            throw new Exception("Missing JobIterationRunInfo in body");
        }

        RunInfo runInfo = jobIterationRunInfo.getRunInfo();
        if (runInfo == null) {
            runInfo = kartaRuntime.getDefaultRunInfo();
        }
        String featureName = jobIterationRunInfo.getFeatureName();
        TestJob job = jobIterationRunInfo.getTestJob();
        int iterationIndex = jobIterationRunInfo.getIterationIndex();

        if (job == null) {
            throw new Exception("Missing job to run in JobIterationRunInfo");
        }

        return TestJobRunner.run(kartaRuntime, runInfo, featureName, job, iterationIndex, null);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST, value = Constants.PATH_RUN_FEATURE)
    public FeatureResult runFeature(@RequestBody FeatureRunInfo featureRunInfo) {
        String featureName = Constants.UNNAMED;
        try {
            if (featureRunInfo == null) {
                return StandardFeatureResults.error(featureName, "Missing FeatureRunInfo in body");
            }

            RunInfo runInfo = featureRunInfo.getRunInfo();
            if (runInfo == null) {
                runInfo = kartaRuntime.getDefaultRunInfo();
            }
            TestFeature feature = featureRunInfo.getTestFeature();

            if (feature == null) {
                return StandardFeatureResults.error(featureName, "Feature to run missing in FeatureRunInfo");
            }

            return kartaRuntime.runFeature(runInfo, feature);
        } catch (Throwable t) {
            return StandardFeatureResults.error(featureName, t);
        }
    }
}
