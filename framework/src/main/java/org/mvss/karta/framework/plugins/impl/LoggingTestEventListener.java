package org.mvss.karta.framework.plugins.impl;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.dependencyinjection.annotations.Initializer;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.models.event.*;
import org.mvss.karta.framework.models.result.FeatureResult;
import org.mvss.karta.framework.models.result.ScenarioResult;
import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.plugins.TestEventListener;

@Log4j2
public class LoggingTestEventListener implements TestEventListener {
    public static final String PLUGIN_NAME = "LoggingTestEventListener";
    public static final String SPACE = " ";
    public static final String STEP = "Step:";
    public static final String STARTED = "started";
    public static final String FAILED = "failed";
    public static final String PASSED = "passed";
    public static final String ERROR = "threw error";
    public static final String COMPLETED = "completed";

    private boolean initialized = false;

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Initializer
    public boolean initialize() throws Throwable {
        if (initialized) {
            return true;
        }

        log.info("Initializing " + PLUGIN_NAME + " plugin");

        initialized = true;
        return true;
    }

    public String getFeatureResultLog(FeatureResult result) {
        return result.isError() ? ERROR : (result.isPassed() ? PASSED : FAILED);
    }

    public String getScenarioResultLog(ScenarioResult result) {
        return result.isError() ? ERROR : (result.isPassed() ? PASSED : FAILED);
    }

    public String getStepResultLog(StepResult result) {
        return result.isError() ? ERROR : (result.isPassed() ? PASSED : FAILED);
    }

    @Override
    public void processEvent(Event event) {
        switch (event.getEventType()) {
            case StandardEventsTypes.RUN_START_EVENT:
                log.info("[{}]" + SPACE + STARTED, event.getRunName());
                break;

            case StandardEventsTypes.RUN_COMPLETE_EVENT:
                log.info("[{}]" + SPACE + COMPLETED, event.getRunName());
                break;

            case StandardEventsTypes.FEATURE_START_EVENT:
                FeatureStartEvent featureStartEvent = (FeatureStartEvent) event;
                log.info("[{}][{}]" + SPACE + STARTED, featureStartEvent.getRunName(), featureStartEvent.getFeature().getName());
                break;

            case StandardEventsTypes.FEATURE_SETUP_STEP_START_EVENT:
                FeatureSetupStepStartEvent featureSetupStepStartEvent = (FeatureSetupStepStartEvent) event;
                log.info("[{}][{}][setup({})[{}]]" + SPACE + STARTED, featureSetupStepStartEvent.getRunName(), featureSetupStepStartEvent.getFeature()
                        .getName(), featureSetupStepStartEvent.getStep().getIdentifier(), featureSetupStepStartEvent.getStep().getIterationIndex());
                break;

            case StandardEventsTypes.FEATURE_SETUP_STEP_COMPLETE_EVENT:
                FeatureSetupStepCompleteEvent featureSetupStepCompleteEvent = (FeatureSetupStepCompleteEvent) event;
                log.info("[{}][{}][setup({})[{}]]" + SPACE + "{}", featureSetupStepCompleteEvent.getRunName(), featureSetupStepCompleteEvent.getFeature()
                        .getName(), featureSetupStepCompleteEvent.getStep().getIdentifier(), featureSetupStepCompleteEvent.getStep().getIterationIndex(), getStepResultLog(
                        featureSetupStepCompleteEvent.getResult()));
                break;

            case StandardEventsTypes.SCENARIO_START_EVENT:
                ScenarioStartEvent scenarioStartEvent = (ScenarioStartEvent) event;
                log.info("[{}][{}][{}][{}]" + SPACE + STARTED, scenarioStartEvent.getRunName(), scenarioStartEvent.getFeatureName(), scenarioStartEvent.getIterationNumber() + 1, scenarioStartEvent.getScenario()
                        .getName());
                break;

            case StandardEventsTypes.SCENARIO_SETUP_STEP_START_EVENT:
                ScenarioSetupStepStartEvent scenarioSetupStepStartEvent = (ScenarioSetupStepStartEvent) event;
                log.info("[{}][{}][{}][{}][setup({})[{}]]" + SPACE + STARTED, scenarioSetupStepStartEvent.getRunName(), scenarioSetupStepStartEvent.getFeatureName(), scenarioSetupStepStartEvent.getScenarioName(), scenarioSetupStepStartEvent.getIterationNumber() + 1, scenarioSetupStepStartEvent.getStep()
                        .getIdentifier(), scenarioSetupStepStartEvent.getStep().getIterationIndex());
                break;
            case StandardEventsTypes.SCENARIO_SETUP_STEP_COMPLETE_EVENT:
                ScenarioSetupStepCompleteEvent scenarioSetupStepCompleteEvent = (ScenarioSetupStepCompleteEvent) event;
                log.info("[{}][{}][{}][{}][setup({})[{}]]" + SPACE + "{}", scenarioSetupStepCompleteEvent.getRunName(), scenarioSetupStepCompleteEvent.getFeatureName(), scenarioSetupStepCompleteEvent.getScenarioName(), scenarioSetupStepCompleteEvent.getIterationNumber() + 1, scenarioSetupStepCompleteEvent.getStep()
                        .getIdentifier(), scenarioSetupStepCompleteEvent.getStep().getIterationIndex(), getStepResultLog(scenarioSetupStepCompleteEvent.getResult()));
                break;

            case StandardEventsTypes.SCENARIO_CHAOS_ACTION_START_EVENT:
                ScenarioChaosActionStartEvent scenarioChaosActionStartEvent = (ScenarioChaosActionStartEvent) event;
                log.info("[{}][{}][{}][{}][chaosAction({})]" + SPACE + STARTED, scenarioChaosActionStartEvent.getRunName(), scenarioChaosActionStartEvent.getFeatureName(), scenarioChaosActionStartEvent.getScenarioName(), scenarioChaosActionStartEvent.getIterationNumber() + 1, scenarioChaosActionStartEvent.getPreparedChaosAction()
                        .getName());
                break;
            case StandardEventsTypes.SCENARIO_CHAOS_ACTION_COMPLETE_EVENT:
                ScenarioChaosActionCompleteEvent scenarioChaosActionCompleteEvent = (ScenarioChaosActionCompleteEvent) event;
                log.info("[{}][{}][{}][{}][chaosAction({})]" + SPACE + "{}", scenarioChaosActionCompleteEvent.getRunName(), scenarioChaosActionCompleteEvent.getFeatureName(), scenarioChaosActionCompleteEvent.getScenarioName(), scenarioChaosActionCompleteEvent.getIterationNumber() + 1, scenarioChaosActionCompleteEvent.getPreparedChaosAction()
                        .getName(), getStepResultLog(scenarioChaosActionCompleteEvent.getResult()));
                break;

            case StandardEventsTypes.SCENARIO_STEP_START_EVENT:
                ScenarioStepStartEvent scenarioStepStartEvent = (ScenarioStepStartEvent) event;
                log.info("[{}][{}][{}][{}][step({})[{}]]" + SPACE + STARTED, scenarioStepStartEvent.getRunName(), scenarioStepStartEvent.getFeatureName(), scenarioStepStartEvent.getScenarioName(), scenarioStepStartEvent.getIterationNumber() + 1, scenarioStepStartEvent.getStep()
                        .getIdentifier(), scenarioStepStartEvent.getStep().getIterationIndex());
                break;

            case StandardEventsTypes.SCENARIO_STEP_COMPLETE_EVENT:
                ScenarioStepCompleteEvent scenarioStepCompleteEvent = (ScenarioStepCompleteEvent) event;
                log.info("[{}][{}][{}][{}][step({})[{}]]" + SPACE + "{}", scenarioStepCompleteEvent.getRunName(), scenarioStepCompleteEvent.getFeatureName(), scenarioStepCompleteEvent.getScenarioName(), scenarioStepCompleteEvent.getIterationNumber() + 1, scenarioStepCompleteEvent.getStep()
                        .getIdentifier(), scenarioStepCompleteEvent.getStep().getIterationIndex(), getStepResultLog(scenarioStepCompleteEvent.getResult()));
                break;

            case StandardEventsTypes.SCENARIO_TEARDOWN_STEP_START_EVENT:
                ScenarioTearDownStepStartEvent scenarioTearDownStepStartEvent = (ScenarioTearDownStepStartEvent) event;
                log.info("[{}][{}][{}][{}][tearDown({})[{}]]" + SPACE + STARTED, scenarioTearDownStepStartEvent.getRunName(), scenarioTearDownStepStartEvent.getFeatureName(), scenarioTearDownStepStartEvent.getScenarioName(), scenarioTearDownStepStartEvent.getIterationNumber() + 1, scenarioTearDownStepStartEvent.getStep()
                        .getIdentifier(), scenarioTearDownStepStartEvent.getStep().getIterationIndex());
                break;

            case StandardEventsTypes.SCENARIO_TEARDOWN_STEP_COMPLETE_EVENT:
                ScenarioTearDownStepCompleteEvent scenarioTearDownStepCompleteEvent = (ScenarioTearDownStepCompleteEvent) event;
                log.info("[{}][{}][{}][{}][tearDown({})[{}]]" + SPACE + "{}", scenarioTearDownStepCompleteEvent.getRunName(), scenarioTearDownStepCompleteEvent.getFeatureName(), scenarioTearDownStepCompleteEvent.getScenarioName(), scenarioTearDownStepCompleteEvent.getIterationNumber() + 1, scenarioTearDownStepCompleteEvent.getStep()
                        .getIdentifier(), scenarioTearDownStepCompleteEvent.getStep().getIterationIndex(), getStepResultLog(scenarioTearDownStepCompleteEvent.getResult()));
                break;

            case StandardEventsTypes.SCENARIO_COMPLETE_EVENT:
                ScenarioCompleteEvent scenarioCompleteEvent = (ScenarioCompleteEvent) event;
                log.info("[{}][{}][{}][{}]" + SPACE + COMPLETED, scenarioCompleteEvent.getRunName(), scenarioCompleteEvent.getFeatureName(), scenarioCompleteEvent.getIterationNumber() + 1, scenarioCompleteEvent.getScenario()
                        .getName());
                break;
            case StandardEventsTypes.FEATURE_TEARDOWN_STEP_START_EVENT:
                FeatureTearDownStepStartEvent featureTearDownStepStartEvent = (FeatureTearDownStepStartEvent) event;
                log.info("[{}][{}][tearDown({})[{}]]" + SPACE + STARTED, featureTearDownStepStartEvent.getRunName(), featureTearDownStepStartEvent.getFeature()
                        .getName(), featureTearDownStepStartEvent.getStep().getIdentifier(), featureTearDownStepStartEvent.getStep().getIterationIndex());
                break;

            case StandardEventsTypes.FEATURE_TEARDOWN_STEP_COMPLETE_EVENT:
                FeatureTearDownStepCompleteEvent featureTearDownStepCompleteEvent = (FeatureTearDownStepCompleteEvent) event;
                log.info("[{}][{}][tearDown({})[{}]]" + SPACE + "{}", featureTearDownStepCompleteEvent.getRunName(), featureTearDownStepCompleteEvent.getFeature()
                        .getName(), featureTearDownStepCompleteEvent.getStep().getIdentifier(), featureTearDownStepCompleteEvent.getStep().getIterationIndex(), getStepResultLog(
                        featureTearDownStepCompleteEvent.getResult()));
                break;

            case StandardEventsTypes.FEATURE_COMPLETE_EVENT:
                FeatureCompleteEvent featureCompleteEvent = (FeatureCompleteEvent) event;
                log.info("[{}][{}]" + SPACE + "{}", featureCompleteEvent.getRunName(), featureCompleteEvent.getFeature().getName(), getFeatureResultLog(
                        featureCompleteEvent.getResult()));
                break;

            case StandardEventsTypes.JAVA_FEATURE_START_EVENT:
                JavaFeatureStartEvent javaFeatureStartEvent = (JavaFeatureStartEvent) event;
                log.info("[{}][{}]" + SPACE + STARTED, javaFeatureStartEvent.getRunName(), javaFeatureStartEvent.getFeatureName());
                break;

            case StandardEventsTypes.JAVA_FEATURE_SETUP_START_EVENT:
                JavaFeatureSetupStartEvent javaFeatureSetupStartEvent = (JavaFeatureSetupStartEvent) event;
                log.info("[{}][{}][setup({})]" + SPACE + STARTED, javaFeatureSetupStartEvent.getRunName(), javaFeatureSetupStartEvent.getFeatureName(), javaFeatureSetupStartEvent.getStepIdentifier());
                break;

            case StandardEventsTypes.JAVA_FEATURE_SETUP_COMPLETE_EVENT:
                JavaFeatureSetupCompleteEvent javaFeatureSetupCompleteEvent = (JavaFeatureSetupCompleteEvent) event;
                log.info("[{}][{}][setup({})]" + SPACE + "{}", javaFeatureSetupCompleteEvent.getRunName(), javaFeatureSetupCompleteEvent.getFeatureName(), javaFeatureSetupCompleteEvent.getStepIdentifier(), getStepResultLog(
                        javaFeatureSetupCompleteEvent.getResult()));
                break;

            case StandardEventsTypes.JAVA_SCENARIO_SETUP_START_EVENT:
                JavaScenarioSetupStartEvent javaScenarioSetupStartEvent = (JavaScenarioSetupStartEvent) event;
                log.info("[{}][{}][{}][{}][setup({})]" + SPACE + STARTED, javaScenarioSetupStartEvent.getRunName(), javaScenarioSetupStartEvent.getFeatureName(), javaScenarioSetupStartEvent.getScenarioName(), javaScenarioSetupStartEvent.getIterationNumber() + 1, javaScenarioSetupStartEvent.getStepIdentifier());
                break;

            case StandardEventsTypes.JAVA_SCENARIO_SETUP_COMPLETE_EVENT:
                JavaScenarioSetupCompleteEvent javaScenarioSetupCompleteEvent = (JavaScenarioSetupCompleteEvent) event;
                log.info("[{}][{}][{}][{}][setup({})]" + SPACE + "{}", javaScenarioSetupCompleteEvent.getRunName(), javaScenarioSetupCompleteEvent.getFeatureName(), javaScenarioSetupCompleteEvent.getScenarioName(), javaScenarioSetupCompleteEvent.getIterationNumber() + 1, javaScenarioSetupCompleteEvent.getStepIdentifier(), getStepResultLog(
                        javaScenarioSetupCompleteEvent.getResult()));
                break;

            case StandardEventsTypes.JAVA_SCENARIO_CHAOS_ACTION_START_EVENT:
                JavaScenarioChaosActionStartEvent javaScenarioChaosActionStartEvent = (JavaScenarioChaosActionStartEvent) event;
                log.info("[{}][{}][{}][{}][chaosAction({})]" + SPACE + STARTED, javaScenarioChaosActionStartEvent.getRunName(), javaScenarioChaosActionStartEvent.getFeatureName(), javaScenarioChaosActionStartEvent.getScenarioName(), javaScenarioChaosActionStartEvent.getIterationNumber() + 1, javaScenarioChaosActionStartEvent.getChaosAction()
                        .getName());
                break;

            case StandardEventsTypes.JAVA_SCENARIO_CHAOS_ACTION_COMPLETE_EVENT:
                JavaScenarioChaosActionCompleteEvent javaScenarioChaosActionCompleteEvent = (JavaScenarioChaosActionCompleteEvent) event;
                log.info("[{}][{}][{}][{}][chaosAction({})]" + SPACE + "{}", javaScenarioChaosActionCompleteEvent.getRunName(), javaScenarioChaosActionCompleteEvent.getFeatureName(), javaScenarioChaosActionCompleteEvent.getScenarioName(), javaScenarioChaosActionCompleteEvent.getIterationNumber() + 1, javaScenarioChaosActionCompleteEvent.getChaosAction()
                        .getName(), getStepResultLog(javaScenarioChaosActionCompleteEvent.getResult()));
                break;

            case StandardEventsTypes.JAVA_SCENARIO_START_EVENT:
                JavaScenarioStartEvent javaScenarioStartEvent = (JavaScenarioStartEvent) event;
                log.info("[{}][{}][{}][{}]" + SPACE + STARTED, javaScenarioStartEvent.getRunName(), javaScenarioStartEvent.getFeatureName(), javaScenarioStartEvent.getScenarioName(), javaScenarioStartEvent.getIterationNumber() + 1);
                break;

            case StandardEventsTypes.JAVA_SCENARIO_COMPLETE_EVENT:
                JavaScenarioCompleteEvent javaScenarioCompleteEvent = (JavaScenarioCompleteEvent) event;
                log.info("[{}][{}][{}][{}]" + SPACE + "{}", javaScenarioCompleteEvent.getRunName(), javaScenarioCompleteEvent.getFeatureName(), javaScenarioCompleteEvent.getScenarioName(), javaScenarioCompleteEvent.getIterationNumber() + 1, getScenarioResultLog(
                        javaScenarioCompleteEvent.getResult()));
                break;

            case StandardEventsTypes.JAVA_SCENARIO_TEARDOWN_START_EVENT:
                JavaScenarioTearDownStartEvent javaScenarioTearDownStartEvent = (JavaScenarioTearDownStartEvent) event;
                log.info("[{}][{}][{}][{}][tearDown({})]" + SPACE + STARTED, javaScenarioTearDownStartEvent.getRunName(), javaScenarioTearDownStartEvent.getFeatureName(), javaScenarioTearDownStartEvent.getScenarioName(), javaScenarioTearDownStartEvent.getIterationNumber() + 1, javaScenarioTearDownStartEvent.getStepIdentifier());
                break;

            case StandardEventsTypes.JAVA_SCENARIO_TEARDOWN_COMPLETE_EVENT:
                JavaScenarioTearDownCompleteEvent javaScenarioTearDownCompleteEvent = (JavaScenarioTearDownCompleteEvent) event;
                log.info("[{}][{}][{}][{}][tearDown({})]" + SPACE + "{}", javaScenarioTearDownCompleteEvent.getRunName(), javaScenarioTearDownCompleteEvent.getFeatureName(), javaScenarioTearDownCompleteEvent.getScenarioName(), javaScenarioTearDownCompleteEvent.getIterationNumber() + 1, javaScenarioTearDownCompleteEvent.getStepIdentifier(), getStepResultLog(
                        javaScenarioTearDownCompleteEvent.getResult()));
                break;

            case StandardEventsTypes.JAVA_FEATURE_TEARDOWN_START_EVENT:
                JavaFeatureTearDownCompleteEvent javaFeatureTearDownCompleteEvent = (JavaFeatureTearDownCompleteEvent) event;
                log.info("[{}][{}][tearDown({})]" + SPACE + "{}", javaFeatureTearDownCompleteEvent.getRunName(), javaFeatureTearDownCompleteEvent.getFeatureName(), javaFeatureTearDownCompleteEvent.getStepIdentifier(), getStepResultLog(
                        javaFeatureTearDownCompleteEvent.getResult()));
                break;

            case StandardEventsTypes.JAVA_FEATURE_TEARDOWN_COMPLETE_EVENT:
                JavaFeatureTearDownStartEvent javaFeatureTearDownStartEvent = (JavaFeatureTearDownStartEvent) event;
                log.info("[{}][{}][tearDown({})]" + SPACE + STARTED, javaFeatureTearDownStartEvent.getRunName(), javaFeatureTearDownStartEvent.getFeatureName(), javaFeatureTearDownStartEvent.getStepIdentifier());

                break;

            case StandardEventsTypes.JAVA_FEATURE_COMPLETE_EVENT:
                JavaFeatureCompleteEvent javaFeatureCompleteEvent = (JavaFeatureCompleteEvent) event;
                log.info("[{}][{}]" + SPACE + COMPLETED, javaFeatureCompleteEvent.getRunName(), javaFeatureCompleteEvent.getFeatureName());
                break;

            case StandardEventsTypes.JOB_STEP_START_EVENT:
                JobStepStartEvent jobStepStartEvent = (JobStepStartEvent) event;
                log.info("[{}][{}][{}][{}][step({})]" + SPACE + STARTED, jobStepStartEvent.getRunName(), jobStepStartEvent.getFeatureName(), jobStepStartEvent.getJob()
                        .getName(), jobStepStartEvent.getIterationNumber() + 1, jobStepStartEvent.getStep()
                        .getStep());
                break;

            case StandardEventsTypes.JOB_STEP_COMPLETE_EVENT:
                JobStepCompleteEvent jobStepCompleteEvent = (JobStepCompleteEvent) event;
                log.info("[{}][{}][{}][{}][step({})]" + SPACE + "{}", jobStepCompleteEvent.getRunName(), jobStepCompleteEvent.getFeatureName(), jobStepCompleteEvent.getJob()
                        .getName(), jobStepCompleteEvent.getIterationNumber() + 1, jobStepCompleteEvent.getStep()
                        .getStep(), getStepResultLog(jobStepCompleteEvent.getResult()));
                break;

            case StandardEventsTypes.CHAOS_ACTION_JOB_COMPLETE_EVENT:
                ChaosActionJobCompleteEvent chaosActionJobCompleteEvent = (ChaosActionJobCompleteEvent) event;
                log.info("[{}][{}][{}][{}][step({})]" + SPACE + "{}", chaosActionJobCompleteEvent.getRunName(), chaosActionJobCompleteEvent.getFeatureName(), chaosActionJobCompleteEvent.getJob()
                        .getName(), chaosActionJobCompleteEvent.getIterationNumber() + 1, chaosActionJobCompleteEvent.getChaosAction()
                        .getName(), getStepResultLog(chaosActionJobCompleteEvent.getResult()));
                break;

            case StandardEventsTypes.CHAOS_ACTION_JOB_START_EVENT:
                ChaosActionJobStartEvent chaosActionJobStartEvent = (ChaosActionJobStartEvent) event;
                log.info("[{}][{}][{}][{}][step({})]" + SPACE + STARTED, chaosActionJobStartEvent.getRunName(), chaosActionJobStartEvent.getFeatureName(), chaosActionJobStartEvent.getJob()
                        .getName(), chaosActionJobStartEvent.getIterationNumber() + 1, chaosActionJobStartEvent.getChaosAction()
                        .getName());
                break;

            case StandardEventsTypes.TEST_INCIDENT_OCCURRENCE_EVENT:
                TestIncidentOccurrenceEvent testIncidentOccurrenceEvent = (TestIncidentOccurrenceEvent) event;
                log.info("Incident occurred: {}", testIncidentOccurrenceEvent.getIncident());
                break;

            // for cases StandardEventsTypes.UNDEFINED and StandardEventsTypes.GENERIC_TEST_EVENT:
            default:
                log.info("[{}][{}" + SPACE + "{}", event.getRunName(), event.getEventType(), event);
                break;
        }
    }
}
