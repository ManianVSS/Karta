package org.mvss.cucumber.extensions;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.*;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.dependencyinjection.Configurator;
import org.mvss.karta.dependencyinjection.KartaDependencyInjector;
import org.mvss.karta.dependencyinjection.annotations.PropertyMapping;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.framework.restclient.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class ShaniDashboardPlugin implements ConcurrentEventListener {

    private static final String PLUGIN_NAME = "ShaniDashboardPlugin";

    public static final String EXECUTION_API_RELEASES = "/execution/api/releases/";
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

    @PropertyMapping(group = PLUGIN_NAME)
    private String releaseName;

    @PropertyMapping(group = PLUGIN_NAME)
    private String build;

    @PropertyMapping(group = PLUGIN_NAME)
    private String runName;

    @PropertyMapping(group = PLUGIN_NAME)
    private String dashboardBaseURL;

    @PropertyMapping(group = PLUGIN_NAME)
    private String dashboardUserName;

    @PropertyMapping(group = PLUGIN_NAME)
    private String dashboardUserPassword;

    private Integer releaseId;
    private Integer runId;

    public ShaniDashboardPlugin(String parameter) {
        log.info("Plugin parameter passed is " + parameter);
        Configurator configurator = new Configurator();
        configurator.mergePropertiesFiles(parameter);
        configurator.loadProperties(this);
        log.info("Initializing " + PLUGIN_NAME + "plugin");
        apacheRestClient = new ApacheRestClient(dashboardBaseURL, true);
    }

    private ApacheRestClient apacheRestClient;

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        KartaDependencyInjector kartaDependencyInjector = KartaDependencyInjector.getInstance();
        kartaDependencyInjector.injectIntoObject(this);
        publisher.registerHandlerFor(TestRunStarted.class, this::handleTestRunStarted);

        publisher.registerHandlerFor(TestCaseStarted.class, this::handleTestCaseStarted);
        publisher.registerHandlerFor(TestCaseFinished.class, this::handleTestCaseFinished);
    }

    private HashMap<String, Serializable> findEntity(String subUrl, HashMap<String, Serializable> params) {
        try (RestResponse restResponse = apacheRestClient.get(ApacheRestRequest.requestBuilder().contentType(ContentType.APPLICATION_JSON).accept(ContentType.ALL).basicAuth(dashboardUserName, dashboardUserPassword).params(params).build(), subUrl)) {
            if (restResponse.getStatusCode() != 200) {
                return null;
            }
            HashMap<String, Serializable> responseBody = restResponse.getBodyAs(ParserUtils.genericHashMapObjectType);
            int count = (int) responseBody.get("count");
            if (count <= 0) {
                return null;
            } else {
                return ParserUtils.getObjectMapper().convertValue(((ArrayList<Serializable>) responseBody.get("results")).get(0), ParserUtils.genericHashMapObjectType);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Integer findEntityId(String subUrl, HashMap<String, Serializable> params) {
        HashMap<String, Serializable> entity = findEntity(subUrl, params);
        if (entity == null) {
            return null;
        }
        return (Integer) entity.get("id");
    }

    private HashMap<String, Serializable> createEntity(String subUrl, HashMap<String, Serializable> entity) {
        RestRequest request = ApacheRestRequest.requestBuilder().contentType(ContentType.APPLICATION_JSON).accept(ContentType.ALL).basicAuth(dashboardUserName, dashboardUserPassword).body(entity).build();
        try (RestResponse restResponse = apacheRestClient.post(request, subUrl)) {
            if (restResponse.getStatusCode() != 201) {
                return null;
            }
            return restResponse.getBodyAs(ParserUtils.genericHashMapObjectType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private HashMap<String, Serializable> updateEntity(String subUrl, HashMap<String, Serializable> entity) {
        try (RestResponse restResponse = apacheRestClient.put(ApacheRestRequest.requestBuilder().contentType(ContentType.APPLICATION_JSON).accept(ContentType.ALL).basicAuth(dashboardUserName, dashboardUserPassword).body(entity).build(), subUrl)) {
            if (restResponse.getStatusCode() != 200) {
                return null;
            }
            return restResponse.getBodyAs(ParserUtils.genericHashMapObjectType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Integer createEntityAndGetId(String subUrl, HashMap<String, Serializable> entity) {
        HashMap<String, Serializable> createdEntity = createEntity(subUrl, entity);
        if (createdEntity == null) {
            return null;
        }
        return (Integer) createdEntity.get("id");
    }

    private HashMap<String, Serializable> getExistingOrCreateNewEntity(String subUrl, HashMap<String, Serializable> params, HashMap<String, Serializable> entity) {
        HashMap<String, Serializable> foundEntityId = findEntity(subUrl, params);
        return (foundEntityId == null) ? createEntity(subUrl, entity) : foundEntityId;
    }

    private Integer getExistingOrCreateNewEntityId(String subUrl, HashMap<String, Serializable> params, HashMap<String, Serializable> entity) {
        Integer foundEntityId = findEntityId(subUrl, params);
        return (foundEntityId == null) ? createEntityAndGetId(subUrl, entity) : foundEntityId;
    }

    private synchronized void handleTestRunStarted(TestRunStarted testRunStarted) {
        releaseId = getExistingOrCreateNewEntityId(EXECUTION_API_RELEASES, new HashMap<>() {{
            put(NAME, runName);
        }}, new HashMap<>() {{
            put(NAME, runName);
            put(SUMMARY, runName);
            put(DESCRIPTION, runName);
        }});

        runId = getExistingOrCreateNewEntityId(EXECUTION_API_RUNS, new HashMap<>() {{
            put(RELEASE, releaseId);
            put(NAME, runName);
        }}, new HashMap<>() {{
            put(RELEASE, releaseId);
            put(NAME, runName);
            put(BUILD, build);
        }});
    }

    private HashMap<String, Serializable> getOrCreateTestCase(TestCase testCase) {
        String name = testCase.getName();
        return getExistingOrCreateNewEntity(EXECUTION_API_EXECUTION_RECORDS, new HashMap<>() {{
            put(RUN, runId);
            put(NAME, name);
        }}, new HashMap<>() {{
            put(RUN, runId);
            put(NAME, name);
        }});
    }

    private Integer getOrCreateTestCaseId(TestCase testCase) {
        return (Integer) getOrCreateTestCase(testCase).get("id");
    }

    private final ConcurrentHashMap<TestCase, Integer> testCaseMap = new ConcurrentHashMap<>();

    private void handleTestCaseStarted(TestCaseStarted testCaseStarted) {
        TestCase testCase = testCaseStarted.getTestCase();
        Integer executionRecordId = getOrCreateTestCaseId(testCase);
        testCaseMap.put(testCase, executionRecordId);
        log.info("Execution record created " + executionRecordId + " for test case " + testCase.getName());
    }

    private void handleTestCaseFinished(TestCaseFinished testCaseFinished) {
        TestCase testCase = testCaseFinished.getTestCase();
        Integer executionRecordId = testCaseMap.get(testCase);
        HashMap<String, Serializable> executionRecord = getOrCreateTestCase(testCase);
        executionRecord.put(STATUS, testCaseFinished.getResult().getStatus() == Status.PASSED ? PASS : FAILED);
        testCaseMap.put(testCase, executionRecordId);
        executionRecord = updateEntity(EXECUTION_API_EXECUTION_RECORDS + executionRecordId + "/", executionRecord);
        log.info("Execution record updated " + executionRecord);
    }
}