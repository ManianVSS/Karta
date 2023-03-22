package org.mvss.karta.framework.plugins;

import org.mvss.karta.framework.models.result.ScenarioResult;
import org.mvss.karta.framework.models.test.PreparedScenario;
import org.mvss.karta.framework.models.test.TestFeature;

import java.util.ArrayList;

public interface TestLifeCycleHook extends Plugin {
    boolean runStart(String runName, ArrayList<String> tags);

    boolean featureStart(String runName, TestFeature feature, ArrayList<String> tags);

    boolean scenarioStart(String runName, String featureName, PreparedScenario scenario, ArrayList<String> tags);

    boolean scenarioStop(String runName, String featureName, PreparedScenario scenario, ArrayList<String> tags);

    boolean scenarioFailed(String runName, String featureName, PreparedScenario scenario, ArrayList<String> tags, ScenarioResult result);

    boolean featureStop(String runName, TestFeature feature, ArrayList<String> tags);

    boolean runStop(String runName, ArrayList<String> tags);
}
