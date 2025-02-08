package org.mvss.karta.framework.plugins.impl;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mvss.karta.dependencyinjection.annotations.Initializer;
import org.mvss.karta.dependencyinjection.annotations.PropertyMapping;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.models.event.*;
import org.mvss.karta.framework.models.result.ScenarioResult;
import org.mvss.karta.framework.models.test.TestIncident;
import org.mvss.karta.framework.plugins.TestEventListener;
import org.mvss.karta.framework.restclient.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class ShaniDashboardReliabilityRunPlugin implements TestEventListener {

    private static final String PLUGIN_NAME = "ShaniDashboardReliabilityRunPlugin";

    public static final String EXECUTION_API_RELEASES = "/execution/api/releases/";
    public static final String EXECUTION_API_BUILDS = "/execution/api/builds/";
    public static final String EXECUTION_API_RELIABILITY_RUNS = "/execution/api/reliability_runs/";
    public static final String RELEASE = "release";
    public static final String NAME = "name";
    public static final String BUILD = "build";
    public static final String SUMMARY = "summary";
    public static final String DESCRIPTION = "description";
    public static final String RUN = "run";
    public static final String EXECUTION_API_RELIABILITY_ITERATIONS = "/execution/api/reliability_iterations/";
    public static final String EXECUTION_API_RELIABILITY_INCIDENTS = "/execution/api/reliability_incidents/";
    public static final String STATUS = "status";
    public static final String PASS = "PASSED";
    public static final String FAILED = "FAILED";
    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";
    public static final String ID = "id";
    public static final String SLASH = "/";
    public static final String EXCEPTION_OCCURRED = "Exception occurred: ";
    public static final String TOKEN = "Token";
    public static final String INDEX = "index";
    public static final String ITERATION = "iteration";

    @PropertyMapping(group = PLUGIN_NAME)
    private String releaseName;

    @PropertyMapping(group = PLUGIN_NAME)
    private String build;

    @PropertyMapping(group = PLUGIN_NAME)
    private String dashboardBaseURL;

    @PropertyMapping(group = PLUGIN_NAME)
    private String dashboardUserToken = "<not provided>";

    private ApacheRestClient apacheRestClient;

    private boolean initialized = false;

    private Integer releaseId;
    private Integer buildId;
    private Integer runId;
    private Instant runStartedTime;

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Initializer
    public boolean initialize() {
        if (initialized) {
            return true;
        }

        apacheRestClient = new ApacheRestClient(dashboardBaseURL, true);
        initialized = true;
        return true;
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

    private HashMap<String, Serializable> createReliabilityRunIfMissing(String runName) throws Exception {
        return getExistingOrCreateNewEntity(EXECUTION_API_RELIABILITY_RUNS, new HashMap<>() {{
            put(RELEASE, releaseId);
            put(NAME, runName);
        }}, new HashMap<>() {{
            put(RELEASE, releaseId);
            put(BUILD, buildId);
            put(NAME, runName);
        }});
    }

    private HashMap<String, Serializable> getOrCreateIteration(String name, long index, Instant startTime) throws Exception {
        if (!scenarioMap.containsKey(name)) {
            scenarioMap.put(name, new ConcurrentHashMap<>());
        }
        ConcurrentHashMap<Long, HashMap<String, Serializable>> scenarioIndexMap = scenarioMap.get(name);
        if (scenarioIndexMap.containsKey(index)) {
            return scenarioIndexMap.get(index);
        } else {
            HashMap<String, Serializable> createdIteration = getExistingOrCreateNewEntity(EXECUTION_API_RELIABILITY_ITERATIONS, new HashMap<>() {{
                put(RUN, runId);
                put(NAME, name);
                put(INDEX, index);
            }}, new HashMap<>() {{
                put(RUN, runId);
                put(NAME, name);
                put(INDEX, index);
                put(START_TIME, startTime.toString());
            }});
            ;
            scenarioIndexMap.put(index, createdIteration);
            return createdIteration;
        }
    }

    private Integer getOrCreateIterationId(String name, long index, Instant startTime) throws Exception {
        return (Integer) getOrCreateIteration(name, index, startTime).get(ID);
    }

    private final ConcurrentHashMap<String, ConcurrentHashMap<Long, HashMap<String, Serializable>>> scenarioMap = new ConcurrentHashMap<>();


    private static void extractID(HashMap<String, Serializable> entity, String objectName) {
        if (entity.containsKey(objectName)) {
            Object run_object = entity.get(objectName);

            if (run_object instanceof HashMap) {
                entity.put(objectName, (Serializable) ((HashMap<?, ?>) run_object).get("id"));
            }
        }
    }

    @Override
    public void processEvent(Event event) {
        try {
            String runName = event.getRunName();

            if (StringUtils.isEmpty(runName)) {
                return;
            }
            switch (event.getEventType()) {
                case StandardEventsTypes.RUN_START_EVENT:
                    RunStartEvent testRunStarted = (RunStartEvent) event;
                    releaseId = (Integer) createReleaseIfMissing().get(ID);
                    buildId = (Integer) createBuildIfMissing().get(ID);
                    runId = (Integer) createReliabilityRunIfMissing(runName).get(ID);
                    runStartedTime = testRunStarted.getTimeOfOccurrence().toInstant();
                    break;

                case StandardEventsTypes.SCENARIO_START_EVENT:

                    ScenarioStartEvent scenarioStartEvent = (ScenarioStartEvent) event;
                    String scenarioStartName = scenarioStartEvent.getScenarioName();
                    long iterationStartNumber = scenarioStartEvent.getIterationNumber();
                    Integer startedTestId = getOrCreateIterationId(scenarioStartName, iterationStartNumber, scenarioStartEvent.getTimeOfOccurrence().toInstant());
                    log.info("Iteration created {} for scenario {} for iteration number {}", startedTestId, scenarioStartEvent.getScenarioName(), iterationStartNumber);
                    break;

                case StandardEventsTypes.SCENARIO_COMPLETE_EVENT:
                    ScenarioCompleteEvent scenarioCompleteEvent = (ScenarioCompleteEvent) event;
                    ScenarioResult scenarioResult = scenarioCompleteEvent.getResult();
                    String scenarioEndName = scenarioCompleteEvent.getScenarioName();
                    long iterationEndNumber = scenarioCompleteEvent.getIterationNumber();
                    Instant instant = scenarioCompleteEvent.getTimeOfOccurrence().toInstant();

                    HashMap<String, Serializable> scenarioIterationObj = getOrCreateIteration(scenarioEndName, iterationEndNumber, instant);
                    Integer completedIterationId = (Integer) scenarioIterationObj.get(ID);

                    scenarioIterationObj.put(STATUS, scenarioResult.isPassed() ? PASS : FAILED);
                    scenarioIterationObj.put(END_TIME, instant.toString());

                    extractID(scenarioIterationObj, "run");

                    scenarioIterationObj = updateEntity(EXECUTION_API_RELIABILITY_ITERATIONS + completedIterationId + SLASH, scenarioIterationObj);
                    log.info("Execution record updated {}", scenarioIterationObj);
                    break;

                case StandardEventsTypes.TEST_INCIDENT_OCCURRENCE_EVENT:

                    TestIncidentOccurrenceEvent testIncidentOccurrenceEvent = (TestIncidentOccurrenceEvent) event;

                    String incidentScenarioName = testIncidentOccurrenceEvent.getScenarioName();
                    long incidentScenarioNumber = testIncidentOccurrenceEvent.getIterationNumber();
                    Instant incidentInstant = testIncidentOccurrenceEvent.getTimeOfOccurrence().toInstant();
                    HashMap<String, Serializable> incidentScenarioIteration = getOrCreateIteration(incidentScenarioName, incidentScenarioNumber, incidentInstant);

                    TestIncident testIncident = testIncidentOccurrenceEvent.getIncident();

                    HashMap<String, Serializable> createIncidentObject = createEntity(EXECUTION_API_RELIABILITY_INCIDENTS, new HashMap<>() {{
                        put(RELEASE, releaseId);
                        put(BUILD, buildId);
                        put(RUN, runId);
                        put(ITERATION, incidentScenarioIteration.get(ID));
                        String summary = testIncident.getMessage();

                        if (summary.length() > 256) {
                            summary = summary.substring(0, 256);
                        }

                        put(SUMMARY, summary);
                        String description = "Message:" + testIncident.getMessage() + "\nDate: " + testIncident.getTimeOfOccurrence() + "\nTags:" + testIncident.getTags();
                        if (testIncident.getThrownCause() != null) {
                            description = description + "\n StackTrace:\n" + ExceptionUtils.getStackTrace(testIncident.getThrownCause());
                        }
//                        if (description.length() > 256) {
//                            description = description.substring(0, 256);
//                        }
                        put(DESCRIPTION, description);

                    }});

                    break;

                case StandardEventsTypes.RUN_COMPLETE_EVENT:
                    RunCompleteEvent runCompleteEvent = (RunCompleteEvent) event;
                    releaseId = (Integer) createReleaseIfMissing().get(ID);
                    buildId = (Integer) createBuildIfMissing().get(ID);
                    HashMap<String, Serializable> run = createReliabilityRunIfMissing(runName);
                    runId = (Integer) run.get(ID);
                    run.put(START_TIME, runStartedTime.toString());
                    run.put(END_TIME, runCompleteEvent.getTimeOfOccurrence().toInstant().toString());
                    extractID(run, "release");
                    extractID(run, "build");
                    run = updateEntity(EXECUTION_API_RELIABILITY_RUNS + runId + SLASH, run);
                    log.info("Run record updated {}", run);
                    break;

            }
        } catch (Throwable t) {
            log.error("Exception during event processing: ", t);
        }
    }

}
