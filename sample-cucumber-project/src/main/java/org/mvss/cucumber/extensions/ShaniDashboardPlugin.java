package org.mvss.cucumber.extensions;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.mvss.karta.dependencyinjection.KartaDependencyInjector;
import org.mvss.karta.dependencyinjection.TestProperties;
import org.mvss.karta.dependencyinjection.annotations.PropertyMapping;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.framework.restclient.*;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class ShaniDashboardPlugin implements ConcurrentEventListener {

    private static final String PLUGIN_NAME = "ShaniDashboardPlugin";

    public static final String EXECUTION_API_RELEASES = "/execution/api/releases/";
    public static final String EXECUTION_API_BUILDS = "/execution/api/builds/";
    public static final String EXECUTION_API_RUNS = "/execution/api/runs/";
    public static final String RELEASE = "release";
    public static final String NAME = "name";
    public static final String BUILD = "build";
    public static final String SUMMARY = "summary";
    public static final String DESCRIPTION = "description";
    public static final String RUN = "run";
    public static final String EXECUTION_API_EXECUTION_RECORDS = "/execution/api/execution_records/";
    public static final String STATUS = "status";
    public static final String PASS = "PASS";
    public static final String FAILED = "FAILED";
    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";
    public static final String ID = "id";
    public static final String SLASH = "/";
    public static final String EXCEPTION_OCCURRED = "Exception occurred: ";
    public static final String TOKEN = "Token";

    @PropertyMapping(group = PLUGIN_NAME)
    private String releaseName;

    @PropertyMapping(group = PLUGIN_NAME)
    private String build;

    @PropertyMapping(group = PLUGIN_NAME)
    private String runName;

    @PropertyMapping(group = PLUGIN_NAME)
    private String dashboardBaseURL;

    @PropertyMapping(group = PLUGIN_NAME)
    private String dashboardUserToken = "<not provided>";

    private Integer releaseId;
    private Integer buildId;
    private Integer runId;
    private Instant runStartedTime;

    public ShaniDashboardPlugin(String parameter) {
        log.info("Plugin parameter passed is " + parameter);
        TestProperties testProperties = new TestProperties();
        testProperties.mergePropertiesFiles(parameter);
        testProperties.loadProperties(this);
        log.info("Initializing " + PLUGIN_NAME + "plugin");
        apacheRestClient = new ApacheRestClient(dashboardBaseURL, true);
    }

    private final ApacheRestClient apacheRestClient;

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        KartaDependencyInjector kartaDependencyInjector = KartaDependencyInjector.getInstance();
        kartaDependencyInjector.inject(this);
        publisher.registerHandlerFor(TestRunStarted.class, this::handleTestRunStarted);

        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);

        publisher.registerHandlerFor(TestRunFinished.class, this::handleTestRunFinished);
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, Serializable> findEntity(String subUrl, HashMap<String, Serializable> params) throws Exception {
        RestRequest request = ApacheRestRequest.requestBuilder().contentType(ContentType.APPLICATION_JSON).accept(ContentType.ALL).tokenAuth(TOKEN, dashboardUserToken).params(params).build();
        try (RestResponse restResponse = apacheRestClient.get(request, subUrl)) {
            if (restResponse.getStatusCode() != 200) {
                throw new Exception("API Request GET failed: " + subUrl + " with params " + params + "\nResponse is: " + restResponse);
            }
            HashMap<String, Serializable> responseBody = restResponse.getBodyAs(ParserUtils.genericHashMapObjectType);
            int count = (int) responseBody.get("count");
            if (count <= 0) {
                return null;
            } else {
                return ParserUtils.getObjectMapper().convertValue(((ArrayList<Serializable>) responseBody.get("results")).getFirst(), ParserUtils.genericHashMapObjectType);
            }
        }
    }

    private Integer findEntityId(String subUrl, HashMap<String, Serializable> params) throws Exception {
        HashMap<String, Serializable> entity = findEntity(subUrl, params);
        if (entity == null) {
            return null;
        }
        return (Integer) entity.get(ID);
    }

    private HashMap<String, Serializable> createEntity(String subUrl, HashMap<String, Serializable> entity) throws Exception {
        RestRequest request = ApacheRestRequest.requestBuilder().contentType(ContentType.APPLICATION_JSON).accept(ContentType.ALL).tokenAuth(TOKEN, dashboardUserToken).body(entity).build();
        try (RestResponse restResponse = apacheRestClient.post(request, subUrl)) {
            if (restResponse.getStatusCode() != 201) {
                throw new Exception("API Request POST failed: " + subUrl + " with entity " + entity + "\nResponse is: " + restResponse);
            }
            return restResponse.getBodyAs(ParserUtils.genericHashMapObjectType);
        }
    }

    private HashMap<String, Serializable> updateEntity(String subUrl, HashMap<String, Serializable> entity) throws Exception {
        RestRequest request = ApacheRestRequest.requestBuilder().contentType(ContentType.APPLICATION_JSON).accept(ContentType.ALL).tokenAuth(TOKEN, dashboardUserToken).body(entity).build();
        try (RestResponse restResponse = apacheRestClient.patch(request, subUrl)) {
            if (restResponse.getStatusCode() != 200) {
                throw new Exception("API Request PATCH failed: " + subUrl + " with entity " + entity + "\nResponse is: " + restResponse);
            }
            return restResponse.getBodyAs(ParserUtils.genericHashMapObjectType);
        }
    }

    private Integer createEntityAndGetId(String subUrl, HashMap<String, Serializable> entity) throws Exception {
        HashMap<String, Serializable> createdEntity = createEntity(subUrl, entity);
        if (createdEntity == null) {
            return null;
        }
        return (Integer) createdEntity.get(ID);
    }

    @SuppressWarnings("SameParameterValue")
    private HashMap<String, Serializable> getExistingOrCreateNewEntity(String subUrl, HashMap<String, Serializable> params, HashMap<String, Serializable> entity) throws Exception {
        HashMap<String, Serializable> foundEntityId = findEntity(subUrl, params);
        return (foundEntityId == null) ? createEntity(subUrl, entity) : foundEntityId;
    }

    private Integer getExistingOrCreateNewEntityId(String subUrl, HashMap<String, Serializable> params, HashMap<String, Serializable> entity) throws Exception {
        Integer foundEntityId = findEntityId(subUrl, params);
        return (foundEntityId == null) ? createEntityAndGetId(subUrl, entity) : foundEntityId;
    }

    private synchronized void handleTestRunStarted(TestRunStarted testRunStarted) {
        try {
            releaseId = (Integer) createReleaseIfMissing().get(ID);
            buildId = (Integer) createBuildIfMissing().get(ID);
            runId = (Integer) createRunIfMissing().get(ID);
            runStartedTime = testRunStarted.getInstant();
        } catch (Exception e) {
            log.error(EXCEPTION_OCCURRED, e);
        }
    }

    private HashMap<String, Serializable> createReleaseIfMissing() throws Exception {
        return getExistingOrCreateNewEntity(EXECUTION_API_RELEASES, new HashMap<>() {{
            put(NAME, releaseName);
        }}, new HashMap<>() {{
            put(NAME, releaseName);
            put(SUMMARY, releaseName);
            put(DESCRIPTION, releaseName);
        }});
    }

    private HashMap<String, Serializable> createBuildIfMissing() throws Exception {
        return getExistingOrCreateNewEntity(EXECUTION_API_BUILDS, new HashMap<>() {{
            put(RELEASE, releaseId);
            put(NAME, build);
        }}, new HashMap<>() {{
            put(RELEASE, releaseId);
            put(NAME, build);
            put(SUMMARY, build);
            put(DESCRIPTION, build);
        }});
    }

    private HashMap<String, Serializable> createRunIfMissing() throws Exception {
        return getExistingOrCreateNewEntity(EXECUTION_API_RUNS, new HashMap<>() {{
            put(RELEASE, releaseId);
            put(NAME, runName);
        }}, new HashMap<>() {{
            put(RELEASE, releaseId);
            put(BUILD, buildId);
            put(NAME, runName);
        }});
    }

    private HashMap<String, Serializable> getOrCreateTestCase(TestCase testCase, Instant startTime) throws Exception {
        String name = testCase.getName();
        List<String> lines = FileUtils.readLines(new File(testCase.getUri()), Charset.defaultCharset());
        StringBuilder testCaseDescription = new StringBuilder(lines.get(testCase.getLocation().getLine() - 1));
        for (int i = testCase.getLocation().getLine(); i < lines.size(); i++) {
            if (lines.get(i).trim().startsWith("Scenario"))
                break;
            testCaseDescription.append("\n").append(lines.get(i));
        }
        String finalTestCaseDescription = testCaseDescription.toString().trim();
        return getExistingOrCreateNewEntity(EXECUTION_API_EXECUTION_RECORDS, new HashMap<>() {{
            put(RUN, runId);
            put(NAME, name);
        }}, new HashMap<>() {{
            put(RUN, runId);
            put(NAME, name);
            put(START_TIME, startTime.toString());
            put(SUMMARY, testCase.getUri());
            put(DESCRIPTION, finalTestCaseDescription);
        }});
    }

    private Integer getOrCreateTestCaseId(TestCase testCase, Instant startTime) throws Exception {
        return (Integer) getOrCreateTestCase(testCase, startTime).get(ID);
    }

    private final ConcurrentHashMap<TestCase, Integer> testCaseMap = new ConcurrentHashMap<>();

    private void handleTestCaseStarted(TestCaseStarted testCaseStarted) {
        try {
            TestCase testCase = testCaseStarted.getTestCase();
            Integer executionRecordId = getOrCreateTestCaseId(testCase, testCaseStarted.getInstant());
            testCaseMap.put(testCase, executionRecordId);
            log.info("Execution record created " + executionRecordId + " for test case " + testCase.getName());
        } catch (Exception e) {
            log.error(EXCEPTION_OCCURRED, e);
        }
    }

    private static void extractID(HashMap<String, Serializable> entity, String objectName) {
        if (entity.containsKey(objectName)) {
            Object run_object = entity.get(objectName);

            if (run_object instanceof HashMap) {
                entity.put(objectName, (Serializable) ((HashMap<?, ?>) run_object).get("id"));
            }
        }
    }

    private void handleTestCaseFinished(TestCaseFinished testCaseFinished) {
        try {
            TestCase testCase = testCaseFinished.getTestCase();
            Integer executionRecordId = testCaseMap.containsKey(testCase) ? testCaseMap.get(testCase) : getOrCreateTestCaseId(testCase, testCaseFinished.getInstant());
            HashMap<String, Serializable> executionRecord = getOrCreateTestCase(testCase, testCaseFinished.getInstant());
            executionRecord.put(STATUS, testCaseFinished.getResult().getStatus() == Status.PASSED ? PASS : FAILED);
            executionRecord.put(END_TIME, testCaseFinished.getInstant().toString());

            extractID(executionRecord, "run");

            testCaseMap.put(testCase, executionRecordId);
            executionRecord = updateEntity(EXECUTION_API_EXECUTION_RECORDS + executionRecordId + SLASH, executionRecord);
            log.info("Execution record updated " + executionRecord);
        } catch (Exception e) {
            log.error(EXCEPTION_OCCURRED, e);
        }
    }

    public synchronized void handleTestRunFinished(TestRunFinished testRunFinished) {
        try {
            releaseId = (Integer) createReleaseIfMissing().get(ID);
            buildId = (Integer) createBuildIfMissing().get(ID);
            HashMap<String, Serializable> run = createRunIfMissing();
            runId = (Integer) run.get(ID);
            run.put(START_TIME, runStartedTime.toString());
            run.put(END_TIME, testRunFinished.getInstant().toString());
            extractID(run, "release");
            extractID(run, "build");
            run = updateEntity(EXECUTION_API_RUNS + runId + SLASH, run);
            log.info("Run record updated " + run);

        } catch (Exception e) {
            log.error(EXCEPTION_OCCURRED, e);
        }
    }

}
