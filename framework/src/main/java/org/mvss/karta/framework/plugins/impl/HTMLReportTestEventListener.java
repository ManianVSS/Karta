package org.mvss.karta.framework.plugins.impl;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.annotations.Initializer;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.models.event.*;
import org.mvss.karta.framework.models.generic.SerializableKVP;
import org.mvss.karta.framework.models.result.FeatureResult;
import org.mvss.karta.framework.models.result.RunResult;
import org.mvss.karta.framework.models.result.ScenarioResult;
import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.models.test.TestFeature;
import org.mvss.karta.framework.models.test.TestIncident;
import org.mvss.karta.framework.models.test.TestScenario;
import org.mvss.karta.framework.plugins.TestEventListener;
import org.mvss.karta.framework.properties.PropertyMapping;
import org.mvss.karta.framework.utils.ParserUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map.Entry;

@Log4j2
public class HTMLReportTestEventListener implements TestEventListener {
    public static final String PLUGIN_NAME = "HTMLReportTestEventListener";
    private static final String _CHAOS = "_CHAOS";
    private static final String _SETUP = "_SETUP";
    private static final String _TEARDOWN = "_TEARDOWN";
    private static final String NA = "NA";
    private static final String STEP_IDENTIFIER = "Step identifier";
    private static final String TIME_OF_OCCURRENCE = "Time of occurrence";
    private static final String THROWN_CAUSE = "Thrown Cause";
    private static final String MESSAGE = "Message";
    private static final String TAGS = "Tags";
    private static final String B_INCIDENTS_B = "<b>Incidents</b>: \r\n";
    private static final String B_ERROR_B = "<b>Error</b>: ";
    private static final String B_SUCCESSFUL_B = "<b>Successful</b>: ";
    private static final String B_END_TIME_B = "<b>EndTime</b>: ";
    private static final String B_START_TIME_B = "<b>StartTime</b>: ";
    private static final String B_FAILED_ITERATIONS_B = "<b>FailedIterations</b>: ";
    private static final String DOT_JSON = ".json";
    private static final String DOT_HTML = ".html";
    private static final String HTML_HEAD = "<!DOCTYPE html>\r\n" + "<html>\r\n" + "<head>\r\n" + "  <title>%s" + "</title>\r\n" + "<style>\r\n" + "table, th, td {\r\n" + "  border: 1px solid black;\r\n" + "  border-collapse: collapse;\r\n" + "}\r\n" + "th, td {\r\n" + "  padding: 5px;\r\n" + "}\r\n" + "th {\r\n" + "  text-align: left;\r\n" + "}</style></head>\r\n" + "<body>";
    private static final String TH_BGCOLOR_BLUE = "    <th bgcolor=\"blue\">";
    private static final String CLOSE_TH = "</th>\r\n";
    private static final String TIME_TAKEN = "TimeTaken";
    private static final String STATUS = "Status";
    private static final String DOT_HTML_CLOSE_TAG = ".html\">";
    private static final String OPEN_TD = "    <td>";
    private static final String BR = "</br>";
    private static final String CLOSE_A = "</a>";
    private static final String OPEN_TABLE = "<table>\r\n";
    private static final String CLOSE_TABLE = "</table>\r\n";
    private static final String SPACE_TAB = "    ";
    private static final String OPEN_TR = "  <tr>\r\n";
    private static final String CLOSE_TR = "  </tr>";
    private static final String CLOSE_TD = "</td>\r\n";
    private static final String A_HREF = "<a href=\"";
    private static final String CLOSE_BODY_AND_HTML = "</body>\r\n" + "</html>";
    private static final String TD_BGCOLOR = "<td bgcolor=\"";
    private static final String QUOTE_CLOSE_TAG = "\">";
    private static final String RED = "red";
    private static final String GREEN = "green";
    @PropertyMapping(group = PLUGIN_NAME)
    private String runReportsBaseFolderName = "reports";

    private File runReportsBaseFolder = null;

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

        runReportsBaseFolder = new File(runReportsBaseFolderName);

        if (!runReportsBaseFolder.exists()) {
            if (!runReportsBaseFolder.mkdirs()) {
                log.error("Could not create report base directory");
                return false;
            }
        } else {
            if (!runReportsBaseFolder.isDirectory()) {
                log.error(runReportsBaseFolderName + " is not a directory");
                return false;
            }
        }

        initialized = true;
        return true;
    }

    // TODO: Add more details to report like iteration details and links between test-> feature->scenario->iteration->step report
    @Override
    public synchronized void processEvent(Event event) {
        try {
            String runName = event.getRunName();

            if (StringUtils.isEmpty(runName)) {
                return;
            }
            switch (event.getEventType()) {
                case StandardEventsTypes.RUN_START_EVENT:
                    Path path = Paths.get(runReportsBaseFolder.getPath(), runName);
                    File runDirectory = path.toFile();
                    if (!runDirectory.mkdirs()) {
                        log.error("Could not create directory: " + runDirectory.getPath());
                    }
                    break;

                case StandardEventsTypes.RUN_COMPLETE_EVENT:
                    path = Paths.get(runReportsBaseFolder.getPath(), runName, "index.html");
                    File runReportFile = path.toFile();
                    RunCompleteEvent runCompleteEvent = (RunCompleteEvent) event;
                    RunResult runResult = runCompleteEvent.getResult();

                    StringBuilder runReportBuilder = new StringBuilder();
                    runReportBuilder.append(String.format(HTML_HEAD, runName));
                    runReportBuilder.append("<h3>Features</h3>\r\n" + OPEN_TABLE + OPEN_TR + TH_BGCOLOR_BLUE + "Feature" + CLOSE_TH + TH_BGCOLOR_BLUE + STATUS + CLOSE_TH + CLOSE_TR);

                    ArrayList<String> keySet = new ArrayList<>(runResult.getTestResultMap().keySet());
                    Collections.sort(keySet);

                    for (String testName : keySet) {
                        boolean passed = runResult.getTestResultMap().get(testName).isPassed();
                        String tdText = TD_BGCOLOR + (passed ? GREEN : RED) + QUOTE_CLOSE_TAG;
                        runReportBuilder.append(OPEN_TR + SPACE_TAB).append(tdText).append(A_HREF).append(testName).append(DOT_HTML_CLOSE_TAG).append(testName).append(CLOSE_A).append(CLOSE_TD).append(SPACE_TAB).append(tdText).append(passed ? Constants.PASS : Constants.FAIL).append(CLOSE_TD).append(CLOSE_TR);
                    }
                    runReportBuilder.append(CLOSE_TABLE + CLOSE_BODY_AND_HTML);
                    FileUtils.write(runReportFile, runReportBuilder.toString(), Charset.defaultCharset());

                    break;

                case StandardEventsTypes.FEATURE_COMPLETE_EVENT:
                case StandardEventsTypes.JAVA_FEATURE_COMPLETE_EVENT:
                    FeatureResult featureResult;
                    String featureName;

                    if (event instanceof FeatureCompleteEvent) {
                        FeatureCompleteEvent featureCompleteEvent = (FeatureCompleteEvent) event;
                        TestFeature feature = featureCompleteEvent.getFeature();
                        featureName = feature.getName();
                        featureResult = featureCompleteEvent.getResult();
                    } else if (event instanceof JavaFeatureCompleteEvent) {
                        JavaFeatureCompleteEvent featureCompleteEvent = (JavaFeatureCompleteEvent) event;
                        TestFeature feature = featureCompleteEvent.getFeature();
                        featureName = feature.getName();
                        featureResult = featureCompleteEvent.getResult();
                    } else {
                        break;
                    }
                    path = Paths.get(runReportsBaseFolder.getPath(), runName, featureName + DOT_HTML);
                    Path jsonPath = Paths.get(runReportsBaseFolder.getPath(), runName, featureName + DOT_JSON);

                    File featureReportFile = path.toFile();
                    File featureJSONDumpFile = jsonPath.toFile();

                    featureResult.sortResults();
                    StringBuilder featureReportBuilder = new StringBuilder();
                    featureReportBuilder.append(String.format(HTML_HEAD, featureName));

                    featureReportBuilder.append(B_START_TIME_B).append(featureResult.getStartTime()).append(BR);
                    featureReportBuilder.append(B_END_TIME_B).append(featureResult.getEndTime()).append(BR);

                    featureReportBuilder.append(B_SUCCESSFUL_B).append(featureResult.isSuccessful()).append(BR);
                    featureReportBuilder.append(B_ERROR_B).append(featureResult.isError()).append(BR);
                    featureReportBuilder.append(B_INCIDENTS_B + OPEN_TABLE + OPEN_TR + TH_BGCOLOR_BLUE + TAGS + CLOSE_TH + TH_BGCOLOR_BLUE + MESSAGE + CLOSE_TH + TH_BGCOLOR_BLUE + THROWN_CAUSE + CLOSE_TH + TH_BGCOLOR_BLUE + TIME_OF_OCCURRENCE + CLOSE_TH + CLOSE_TR);
                    for (TestIncident incident : featureResult.getIncidents()) {
                        featureReportBuilder.append(OPEN_TR + OPEN_TD).append(incident.getTags()).append(CLOSE_TD).append(OPEN_TD).append(incident.getMessage()).append(CLOSE_TD).append(OPEN_TD).append(incident.getThrownCause()).append(CLOSE_TD).append(OPEN_TD).append(incident.getTimeOfOccurrence()).append(CLOSE_TD).append(CLOSE_TR);
                    }
                    featureReportBuilder.append(CLOSE_TABLE);

                    featureReportBuilder.append("<h3>Feature setup</h3>\r\n" + OPEN_TABLE + OPEN_TR + TH_BGCOLOR_BLUE + STEP_IDENTIFIER + CLOSE_TH + TH_BGCOLOR_BLUE + TIME_TAKEN + CLOSE_TH + TH_BGCOLOR_BLUE + STATUS + CLOSE_TH + CLOSE_TR);
                    for (SerializableKVP<String, StepResult> stepResultPair : featureResult.getSetupResults()) {
                        String stepIdentifier = stepResultPair.getKey();
                        StepResult stepResult = stepResultPair.getValue();
                        String timeTaken = (stepResult.getStartTime() == null) || (stepResult.getEndTime() == null) ? NA : Duration.between(stepResult.getStartTime().toInstant(), stepResult.getEndTime().toInstant()).toString();
                        boolean passed = stepResult.isPassed();
                        String tdText = TD_BGCOLOR + (passed ? GREEN : RED) + QUOTE_CLOSE_TAG;
                        featureReportBuilder.append(OPEN_TR + SPACE_TAB).append(tdText).append(A_HREF).append(featureName).append(Constants.SLASH).append(Constants.__FEATURE_SETUP__).append(Constants.SLASH).append(stepResult.getStepIndex()).append(Constants.HYPHEN).append(stepIdentifier.replaceAll(Constants.REGEX_NON_ALPHANUMERIC, Constants.UNDERSCORE)).append(DOT_HTML_CLOSE_TAG).append(stepIdentifier).append(CLOSE_A).append(CLOSE_TD).append(SPACE_TAB).append(tdText).append(timeTaken).append(CLOSE_TD).append(SPACE_TAB).append(tdText).append(passed ? Constants.PASS : Constants.FAIL).append(CLOSE_TD).append(CLOSE_TR);
                    }
                    featureReportBuilder.append(CLOSE_TABLE);

                    featureReportBuilder.append(BR + B_FAILED_ITERATIONS_B).append(featureResult.getFailedIterations()).append(BR);

                    featureReportBuilder.append("<h3>Feature teardown</h3>\r\n" + OPEN_TABLE + OPEN_TR + TH_BGCOLOR_BLUE + STEP_IDENTIFIER + CLOSE_TH + TH_BGCOLOR_BLUE + TIME_TAKEN + CLOSE_TH + TH_BGCOLOR_BLUE + STATUS + CLOSE_TH + CLOSE_TR);
                    for (SerializableKVP<String, StepResult> stepResultPair : featureResult.getTearDownResults()) {
                        String stepIdentifier = stepResultPair.getKey();
                        StepResult stepResult = stepResultPair.getValue();
                        String timeTaken = (stepResult.getStartTime() == null) || (stepResult.getEndTime() == null) ? NA : Duration.between(stepResult.getStartTime().toInstant(), stepResult.getEndTime().toInstant()).toString();
                        boolean passed = stepResult.isPassed();
                        String tdText = TD_BGCOLOR + (passed ? GREEN : RED) + QUOTE_CLOSE_TAG;
                        featureReportBuilder.append(OPEN_TR + SPACE_TAB).append(tdText).append(A_HREF).append(featureName).append(Constants.SLASH).append(Constants.__FEATURE_TEARDOWN__).append(Constants.SLASH).append(stepResult.getStepIndex()).append(Constants.HYPHEN).append(stepIdentifier.replaceAll(Constants.REGEX_NON_ALPHANUMERIC, Constants.UNDERSCORE)).append(DOT_HTML_CLOSE_TAG).append(stepIdentifier).append(CLOSE_A).append(CLOSE_TD).append(SPACE_TAB).append(tdText).append(timeTaken).append(CLOSE_TD).append(SPACE_TAB).append(tdText).append(passed ? Constants.PASS : Constants.FAIL).append(CLOSE_TD).append(CLOSE_TR);
                    }
                    featureReportBuilder.append(CLOSE_BODY_AND_HTML);

                    FileUtils.write(featureReportFile, featureReportBuilder.toString(), Charset.defaultCharset());
                    FileUtils.write(featureJSONDumpFile, ParserUtils.getObjectMapper().writeValueAsString(featureResult), Charset.defaultCharset());

                    break;

                case StandardEventsTypes.FEATURE_SETUP_STEP_COMPLETE_EVENT:
                case StandardEventsTypes.JAVA_FEATURE_SETUP_COMPLETE_EVENT:
                    StepResult result;
                    String stepId;

                    if (event instanceof FeatureSetupStepCompleteEvent) {
                        FeatureSetupStepCompleteEvent featureSetupStepCompleteEvent = (FeatureSetupStepCompleteEvent) event;
                        featureName = featureSetupStepCompleteEvent.getFeatureName();
                        result = featureSetupStepCompleteEvent.getResult();
                        stepId = featureSetupStepCompleteEvent.getStep().getStep();
                    } else if (event instanceof JavaFeatureSetupCompleteEvent) {
                        JavaFeatureSetupCompleteEvent featureSetupStepCompleteEvent = (JavaFeatureSetupCompleteEvent) event;
                        featureName = featureSetupStepCompleteEvent.getFeatureName();
                        result = featureSetupStepCompleteEvent.getResult();
                        stepId = featureSetupStepCompleteEvent.getStepIdentifier();
                    } else {
                        break;
                    }
                    writeStepReport(runName, featureName, -1, Constants.__FEATURE_SETUP__, result, stepId);

                    break;

                case StandardEventsTypes.FEATURE_TEARDOWN_STEP_COMPLETE_EVENT:
                case StandardEventsTypes.JAVA_FEATURE_TEARDOWN_COMPLETE_EVENT:

                    if (event instanceof FeatureTearDownStepCompleteEvent) {
                        FeatureTearDownStepCompleteEvent featureTearDownStepCompleteEvent = (FeatureTearDownStepCompleteEvent) event;
                        featureName = featureTearDownStepCompleteEvent.getFeatureName();
                        result = featureTearDownStepCompleteEvent.getResult();
                        stepId = featureTearDownStepCompleteEvent.getStep().getStep();
                    } else if (event instanceof JavaFeatureTearDownCompleteEvent) {
                        JavaFeatureTearDownCompleteEvent featureTearDownStepCompleteEvent = (JavaFeatureTearDownCompleteEvent) event;
                        featureName = featureTearDownStepCompleteEvent.getFeatureName();
                        result = featureTearDownStepCompleteEvent.getResult();
                        stepId = featureTearDownStepCompleteEvent.getStepIdentifier();
                    } else {
                        break;
                    }
                    writeStepReport(runName, featureName, -1, Constants.__FEATURE_TEARDOWN__, result, stepId);

                    break;

                case StandardEventsTypes.SCENARIO_COMPLETE_EVENT:
                case StandardEventsTypes.JAVA_SCENARIO_COMPLETE_EVENT:
                    ScenarioResult scenarioResult;
                    String scenarioName;
                    long iterationIndex;

                    if (event instanceof ScenarioCompleteEvent) {
                        ScenarioCompleteEvent scenarioCompleteEvent = (ScenarioCompleteEvent) event;
                        TestScenario scenario = scenarioCompleteEvent.getScenario();
                        featureName = scenarioCompleteEvent.getFeatureName();
                        scenarioName = scenario.getName();
                        scenarioResult = scenarioCompleteEvent.getResult();
                        iterationIndex = scenarioCompleteEvent.getIterationNumber();
                    } else if (event instanceof JavaScenarioCompleteEvent) {
                        JavaScenarioCompleteEvent scenarioCompleteEvent = (JavaScenarioCompleteEvent) event;
                        TestScenario scenario = scenarioCompleteEvent.getScenario();
                        featureName = scenarioCompleteEvent.getFeatureName();
                        scenarioName = scenario.getName();
                        scenarioResult = scenarioCompleteEvent.getResult();
                        iterationIndex = scenarioCompleteEvent.getIterationNumber();
                    } else {
                        break;
                    }
                    String iterationFolder = "" + (iterationIndex + 1);
                    Path featureIterationReportDirectory = Paths.get(runReportsBaseFolder.getPath(), runName, featureName, iterationFolder);

                    Files.createDirectories(featureIterationReportDirectory);

                    path = Paths.get(runReportsBaseFolder.getPath(), runName, featureName, iterationFolder, scenarioName + DOT_HTML);
                    jsonPath = Paths.get(runReportsBaseFolder.getPath(), runName, featureName, iterationFolder, scenarioName + DOT_JSON);

                    File scenarioReportFile = path.toFile();
                    File scenarioJSONDumpFile = jsonPath.toFile();

                    StringBuilder scenarioReportBuilder = new StringBuilder();
                    scenarioReportBuilder.append(String.format(HTML_HEAD, scenarioName));

                    // String scenarioFolderPrefix = featureName + Constants.SLASH + ( iterationIndex + 1 ) + Constants.SLASH + scenarioName;

                    scenarioReportBuilder.append("<h3>Scenario setup</h3>\r\n" + OPEN_TABLE + OPEN_TR + TH_BGCOLOR_BLUE + STEP_IDENTIFIER + CLOSE_TH + TH_BGCOLOR_BLUE + TIME_TAKEN + CLOSE_TH + TH_BGCOLOR_BLUE + STATUS + CLOSE_TH + CLOSE_TR);
                    for (SerializableKVP<String, StepResult> stepResultPair : scenarioResult.getSetupResults()) {
                        String stepIdentifier = stepResultPair.getKey();
                        StepResult stepResult = stepResultPair.getValue();
                        String timeTaken = (stepResult.getStartTime() == null) || (stepResult.getEndTime() == null) ? NA : Duration.between(stepResult.getStartTime().toInstant(), stepResult.getEndTime().toInstant()).toString();
                        boolean passed = stepResult.isPassed();
                        String tdText = TD_BGCOLOR + (passed ? GREEN : RED) + QUOTE_CLOSE_TAG;
                        scenarioReportBuilder.append(OPEN_TR + SPACE_TAB).append(tdText).append(A_HREF).append(scenarioName).append(_SETUP).append(Constants.SLASH).append(stepResult.getStepIndex()).append(Constants.HYPHEN).append(stepIdentifier.replaceAll(Constants.REGEX_NON_ALPHANUMERIC, Constants.UNDERSCORE)).append(DOT_HTML_CLOSE_TAG).append(stepIdentifier).append(CLOSE_A).append(CLOSE_TD).append(SPACE_TAB).append(tdText).append(timeTaken).append(CLOSE_TD).append(SPACE_TAB).append(tdText).append(passed ? Constants.PASS : Constants.FAIL).append(CLOSE_TD).append(CLOSE_TR);
                    }
                    scenarioReportBuilder.append(CLOSE_TABLE);

                    if (!scenarioResult.getChaosActionResults().isEmpty()) {
                        scenarioReportBuilder.append("<h3>Scenario chaos actions</h3>\r\n" + OPEN_TABLE + OPEN_TR + TH_BGCOLOR_BLUE + STEP_IDENTIFIER + CLOSE_TH + TH_BGCOLOR_BLUE + TIME_TAKEN + CLOSE_TH + TH_BGCOLOR_BLUE + STATUS + CLOSE_TH + CLOSE_TR);
                        for (SerializableKVP<String, StepResult> stepResultPair : scenarioResult.getChaosActionResults()) {
                            String stepIdentifier = stepResultPair.getKey();
                            StepResult stepResult = stepResultPair.getValue();
                            String timeTaken = (stepResult.getStartTime() == null) || (stepResult.getEndTime() == null) ? NA : Duration.between(stepResult.getStartTime().toInstant(), stepResult.getEndTime().toInstant()).toString();
                            boolean passed = stepResult.isPassed();
                            String tdText = TD_BGCOLOR + (passed ? GREEN : RED) + QUOTE_CLOSE_TAG;
                            scenarioReportBuilder.append(OPEN_TR + SPACE_TAB).append(tdText).append(A_HREF).append(scenarioName).append(_CHAOS).append(Constants.SLASH).append(stepResult.getStepIndex()).append(Constants.HYPHEN).append(stepIdentifier.replaceAll(Constants.REGEX_NON_ALPHANUMERIC, Constants.UNDERSCORE)).append(DOT_HTML_CLOSE_TAG).append(stepIdentifier).append(CLOSE_A).append(CLOSE_TD).append(SPACE_TAB).append(tdText).append(timeTaken).append(CLOSE_TD).append(SPACE_TAB).append(tdText).append(passed ? Constants.PASS : Constants.FAIL).append(CLOSE_TD).append(CLOSE_TR);
                        }
                        scenarioReportBuilder.append(CLOSE_TABLE);
                    }

                    scenarioReportBuilder.append("<h3>Scenario execution</h3>\r\n" + OPEN_TABLE + OPEN_TR + TH_BGCOLOR_BLUE + STEP_IDENTIFIER + CLOSE_TH + TH_BGCOLOR_BLUE + TIME_TAKEN + CLOSE_TH + TH_BGCOLOR_BLUE + STATUS + CLOSE_TH + CLOSE_TR);
                    for (SerializableKVP<String, StepResult> stepResultPair : scenarioResult.getRunResults()) {
                        String stepIdentifier = stepResultPair.getKey();
                        StepResult stepResult = stepResultPair.getValue();
                        String timeTaken = (stepResult.getStartTime() == null) || (stepResult.getEndTime() == null) ? NA : Duration.between(stepResult.getStartTime().toInstant(), stepResult.getEndTime().toInstant()).toString();
                        boolean passed = stepResult.isPassed();
                        String tdText = TD_BGCOLOR + (passed ? GREEN : RED) + QUOTE_CLOSE_TAG;
                        scenarioReportBuilder.append(OPEN_TR + SPACE_TAB).append(tdText).append(A_HREF).append(scenarioName).append(Constants.SLASH).append(stepResult.getStepIndex()).append(Constants.HYPHEN).append(stepIdentifier.replaceAll(Constants.REGEX_NON_ALPHANUMERIC, Constants.UNDERSCORE)).append(DOT_HTML_CLOSE_TAG).append(stepIdentifier).append(CLOSE_A).append(CLOSE_TD).append(SPACE_TAB).append(tdText).append(timeTaken).append(CLOSE_TD).append(SPACE_TAB).append(tdText).append(passed ? Constants.PASS : Constants.FAIL).append(CLOSE_TD).append(CLOSE_TR);
                    }
                    scenarioReportBuilder.append(CLOSE_TABLE);

                    scenarioReportBuilder.append("<h3>Scenario teardown</h3>\r\n" + OPEN_TABLE + OPEN_TR + TH_BGCOLOR_BLUE + STEP_IDENTIFIER + CLOSE_TH + TH_BGCOLOR_BLUE + TIME_TAKEN + CLOSE_TH + TH_BGCOLOR_BLUE + STATUS + CLOSE_TH + CLOSE_TR);
                    for (SerializableKVP<String, StepResult> stepResultPair : scenarioResult.getTearDownResults()) {
                        String stepIdentifier = stepResultPair.getKey();
                        StepResult stepResult = stepResultPair.getValue();
                        String timeTaken = (stepResult.getStartTime() == null) || (stepResult.getEndTime() == null) ? NA : Duration.between(stepResult.getStartTime().toInstant(), stepResult.getEndTime().toInstant()).toString();
                        boolean passed = stepResult.isPassed();
                        String tdText = TD_BGCOLOR + (passed ? GREEN : RED) + QUOTE_CLOSE_TAG;
                        scenarioReportBuilder.append(OPEN_TR + SPACE_TAB).append(tdText).append(A_HREF).append(scenarioName).append(_TEARDOWN).append(Constants.SLASH).append(stepResult.getStepIndex()).append(Constants.HYPHEN).append(stepIdentifier.replaceAll(Constants.REGEX_NON_ALPHANUMERIC, Constants.UNDERSCORE)).append(DOT_HTML_CLOSE_TAG).append(stepIdentifier).append(CLOSE_A).append(CLOSE_TD).append(SPACE_TAB).append(tdText).append(timeTaken).append(CLOSE_TD).append(SPACE_TAB).append(tdText).append(passed ? Constants.PASS : Constants.FAIL).append(CLOSE_TD).append(CLOSE_TR);
                    }
                    scenarioReportBuilder.append(CLOSE_BODY_AND_HTML);

                    FileUtils.write(scenarioReportFile, scenarioReportBuilder.toString(), Charset.defaultCharset());
                    FileUtils.write(scenarioJSONDumpFile, ParserUtils.getObjectMapper().writeValueAsString(scenarioResult), Charset.defaultCharset());

                    break;

                case StandardEventsTypes.SCENARIO_SETUP_STEP_COMPLETE_EVENT:
                case StandardEventsTypes.JAVA_SCENARIO_SETUP_COMPLETE_EVENT:
                    if (event instanceof ScenarioSetupStepCompleteEvent) {
                        ScenarioSetupStepCompleteEvent scenarioSetupStepCompleteEvent = (ScenarioSetupStepCompleteEvent) event;
                        featureName = scenarioSetupStepCompleteEvent.getFeatureName();
                        scenarioName = scenarioSetupStepCompleteEvent.getScenarioName();
                        result = scenarioSetupStepCompleteEvent.getResult();
                        stepId = scenarioSetupStepCompleteEvent.getStep().getIdentifier();
                        iterationIndex = scenarioSetupStepCompleteEvent.getIterationNumber();
                    } else if (event instanceof JavaScenarioSetupCompleteEvent) {
                        JavaScenarioSetupCompleteEvent scenarioSetupStepCompleteEvent = (JavaScenarioSetupCompleteEvent) event;
                        featureName = scenarioSetupStepCompleteEvent.getFeatureName();
                        scenarioName = scenarioSetupStepCompleteEvent.getScenarioName();
                        result = scenarioSetupStepCompleteEvent.getResult();
                        stepId = scenarioSetupStepCompleteEvent.getStepIdentifier();
                        iterationIndex = scenarioSetupStepCompleteEvent.getIterationNumber();
                    } else {
                        break;
                    }
                    writeStepReport(runName, featureName, iterationIndex, scenarioName + _SETUP, result, stepId);

                    break;

                case StandardEventsTypes.SCENARIO_CHAOS_ACTION_COMPLETE_EVENT:

                    if (event instanceof ScenarioChaosActionCompleteEvent) {
                        ScenarioChaosActionCompleteEvent scenarioChaosActionCompleteEvent = (ScenarioChaosActionCompleteEvent) event;
                        featureName = scenarioChaosActionCompleteEvent.getFeatureName();
                        scenarioName = scenarioChaosActionCompleteEvent.getScenarioName();
                        result = scenarioChaosActionCompleteEvent.getResult();
                        stepId = scenarioChaosActionCompleteEvent.getPreparedChaosAction().getName();
                        iterationIndex = scenarioChaosActionCompleteEvent.getIterationNumber();
                    } else {
                        break;
                    }
                    writeStepReport(runName, featureName, iterationIndex, scenarioName + _CHAOS, result, stepId);

                    break;

                case StandardEventsTypes.SCENARIO_STEP_COMPLETE_EVENT:

                    if (event instanceof ScenarioStepCompleteEvent) {
                        ScenarioStepCompleteEvent scenarioStepCompleteEvent = (ScenarioStepCompleteEvent) event;
                        featureName = scenarioStepCompleteEvent.getFeatureName();
                        scenarioName = scenarioStepCompleteEvent.getScenarioName();
                        result = scenarioStepCompleteEvent.getResult();
                        stepId = scenarioStepCompleteEvent.getStep().getIdentifier();
                        iterationIndex = scenarioStepCompleteEvent.getIterationNumber();
                    } else {
                        break;
                    }
                    writeStepReport(runName, featureName, iterationIndex, scenarioName, result, stepId);

                    break;

                case StandardEventsTypes.SCENARIO_TEARDOWN_STEP_COMPLETE_EVENT:
                case StandardEventsTypes.JAVA_SCENARIO_TEARDOWN_COMPLETE_EVENT:

                    if (event instanceof ScenarioTearDownStepCompleteEvent) {
                        ScenarioTearDownStepCompleteEvent scenarioTearDownStepCompleteEvent = (ScenarioTearDownStepCompleteEvent) event;
                        featureName = scenarioTearDownStepCompleteEvent.getFeatureName();
                        scenarioName = scenarioTearDownStepCompleteEvent.getScenarioName();
                        result = scenarioTearDownStepCompleteEvent.getResult();
                        stepId = scenarioTearDownStepCompleteEvent.getStep().getIdentifier();
                        iterationIndex = scenarioTearDownStepCompleteEvent.getIterationNumber();
                    } else if (event instanceof JavaScenarioTearDownCompleteEvent) {
                        JavaScenarioTearDownCompleteEvent scenarioTearDownStepCompleteEvent = (JavaScenarioTearDownCompleteEvent) event;
                        featureName = scenarioTearDownStepCompleteEvent.getFeatureName();
                        scenarioName = scenarioTearDownStepCompleteEvent.getScenarioName();
                        result = scenarioTearDownStepCompleteEvent.getResult();
                        stepId = scenarioTearDownStepCompleteEvent.getStepIdentifier();
                        iterationIndex = scenarioTearDownStepCompleteEvent.getIterationNumber();
                    } else {
                        break;
                    }
                    writeStepReport(runName, featureName, iterationIndex, scenarioName + _TEARDOWN, result, stepId);

                    break;

            }
        } catch (Throwable t) {
            log.error("Exception during event processing: ", t);
        }
    }

    public void writeStepReport(String runName, String featureName, long iterationIndex, String scenarioName, StepResult result, String stepId) throws IOException {
        String iteartionString = "" + (iterationIndex + 1);
        Path featureSetupReportDirectory = (iterationIndex > -1) ? Paths.get(runReportsBaseFolder.getPath(), runName, featureName, iteartionString, scenarioName) : Paths.get(runReportsBaseFolder.getPath(), runName, featureName, scenarioName);
        Files.createDirectories(featureSetupReportDirectory);
        String fileBaseName = "" + result.getStepIndex() + Constants.HYPHEN + stepId.replaceAll(Constants.REGEX_NON_ALPHANUMERIC, Constants.UNDERSCORE);
        Path path = (iterationIndex > -1) ? Paths.get(runReportsBaseFolder.getPath(), runName, featureName, iteartionString, scenarioName, fileBaseName + DOT_HTML) : Paths.get(runReportsBaseFolder.getPath(), runName, featureName, scenarioName, fileBaseName + DOT_HTML);
        Path jsonPath = (iterationIndex > -1) ? Paths.get(runReportsBaseFolder.getPath(), runName, featureName, iteartionString, scenarioName, fileBaseName + DOT_JSON) : Paths.get(runReportsBaseFolder.getPath(), runName, featureName, scenarioName, fileBaseName + DOT_JSON);

        File reportFile = path.toFile();
        File jsonDumpFile = jsonPath.toFile();

        StringBuilder stepReportBuilder = new StringBuilder();

        stepReportBuilder.append(String.format(HTML_HEAD, stepId));
        stepReportBuilder.append(B_START_TIME_B).append(result.getStartTime()).append(BR);
        stepReportBuilder.append(B_END_TIME_B).append(result.getEndTime()).append(BR);
        stepReportBuilder.append(B_SUCCESSFUL_B).append(result.isSuccessful()).append(BR);
        stepReportBuilder.append(B_ERROR_B).append(result.isError()).append(BR);
        stepReportBuilder.append(B_INCIDENTS_B + OPEN_TABLE + OPEN_TR + TH_BGCOLOR_BLUE + TAGS + CLOSE_TH + TH_BGCOLOR_BLUE + MESSAGE + CLOSE_TH + TH_BGCOLOR_BLUE + THROWN_CAUSE + CLOSE_TH + TH_BGCOLOR_BLUE + TIME_OF_OCCURRENCE + CLOSE_TH + CLOSE_TR);
        for (TestIncident incident : result.getIncidents()) {
            stepReportBuilder.append(OPEN_TR + OPEN_TD).append(incident.getTags()).append(CLOSE_TD).append(OPEN_TD).append(incident.getMessage()).append(CLOSE_TD).append(OPEN_TD).append(incident.getThrownCause()).append(CLOSE_TD).append(OPEN_TD).append(incident.getTimeOfOccurrence()).append(CLOSE_TD).append(CLOSE_TR);
        }
        stepReportBuilder.append(CLOSE_TABLE);
        stepReportBuilder.append("<b>Results</b>: \r\n" + OPEN_TABLE + OPEN_TR + TH_BGCOLOR_BLUE + "Result Key" + CLOSE_TH + TH_BGCOLOR_BLUE + "Result value" + CLOSE_TH + CLOSE_TR);
        for (Entry<String, Serializable> resultEntry : result.getResults().entrySet()) {
            stepReportBuilder.append(OPEN_TR + OPEN_TD).append(resultEntry.getKey()).append(CLOSE_TD).append(OPEN_TD).append(ParserUtils.getObjectMapper().writeValueAsString(resultEntry.getValue())).append(CLOSE_TD).append(CLOSE_TR);
        }
        stepReportBuilder.append(CLOSE_TABLE);

        stepReportBuilder.append(CLOSE_BODY_AND_HTML);

        FileUtils.write(reportFile, stepReportBuilder.toString(), Charset.defaultCharset());
        FileUtils.write(jsonDumpFile, ParserUtils.getObjectMapper().writeValueAsString(result), Charset.defaultCharset());
    }
}
