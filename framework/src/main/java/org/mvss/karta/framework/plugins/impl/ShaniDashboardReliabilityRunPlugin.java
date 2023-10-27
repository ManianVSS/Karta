package org.mvss.karta.framework.plugins.impl;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.dependencyinjection.annotations.Initializer;
import org.mvss.karta.dependencyinjection.annotations.PropertyMapping;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.models.event.Event;
import org.mvss.karta.framework.models.event.ScenarioCompleteEvent;
import org.mvss.karta.framework.models.event.ScenarioStartEvent;
import org.mvss.karta.framework.models.result.ScenarioResult;
import org.mvss.karta.framework.plugins.TestEventListener;

import java.io.Serializable;
import java.util.HashMap;

@Log4j2
public class ShaniDashboardReliabilityRunPlugin implements TestEventListener {

    private static final String PLUGIN_NAME = "ShaniDashboardReliabilityRunPlugin";

    @PropertyMapping(group = PLUGIN_NAME, value = "dashBoardAPIServer")
    private String dashBoardAPIServer = "http://localhost:xxxx";

    @PropertyMapping(group = PLUGIN_NAME, value = "targetIPTI")
    private long targetIPTI = 0L;

    @PropertyMapping(group = PLUGIN_NAME, value = "release")
    private String release = "1.X";

    @PropertyMapping(group = PLUGIN_NAME, value = "build")
    private int build = 1;

    @PropertyMapping(group = PLUGIN_NAME, value = "testEnvironmentType")
    private String testEnvironmentType = "ENV_TYPE";

    @PropertyMapping(group = PLUGIN_NAME, value = "testEnvironmentName")
    private String testEnvironmentName = "ENV_NAME";

    private RequestSpecBuilder requestSpecBuilder;

    private boolean initialized = false;
    private static final String subUrl = "/execution/api/reliability_runs";
    private static final String subUrlByNameUpdateIterations = subUrl + "ByNameUpdateIterations";
    private static final String subUrlByNameUpdateIncidents = subUrl + "ByNameUpdateIncidents";
    private static final String subUrlByNameUpdateStatus = subUrl + "ByNameUpdateStatus";

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    @Initializer
    public boolean initialize() {
        if (initialized) {
            return true;
        }

        log.info("Initializing " + PLUGIN_NAME + "plugin");
        requestSpecBuilder = new RequestSpecBuilder();
        requestSpecBuilder.setBaseUri(dashBoardAPIServer).setContentType(ContentType.JSON).setAccept(ContentType.JSON);
        requestSpecBuilder.setRelaxedHTTPSValidation();

        initialized = true;
        return true;
    }

    @Override
    public void processEvent(Event event) {
        try {
            String runName = event.getRunName();

            if (StringUtils.isEmpty(runName)) {
                return;
            }
            switch (event.getEventType()) {
                case StandardEventsTypes.SCENARIO_START_EVENT:
                    ScenarioStartEvent scenarioStartEvent = (ScenarioStartEvent) event;
                    HashMap<String, Serializable> request = new HashMap<>();
                    request.put("release", release);
                    request.put("build", build);
                    request.put("name", scenarioStartEvent.getRunName());
                    request.put("testName", scenarioStartEvent.getFeatureName());
                    request.put("testEnvironmentType", testEnvironmentType);
                    request.put("testEnvironmentName", testEnvironmentName);
                    request.put("targetIPTI", targetIPTI);
                    request.put("totalIterationCount", 0);
                    request.put("passedIterationCount", 0);
                    request.put("incidentCount", 0);
                    request.put("ipte", 0);

                    Response response = RestAssured.given(requestSpecBuilder.build()).body(request).post(subUrl);
                    if (response.getStatusCode() == 201) {
                        log.debug("New execution record for " + runName + " is created on the dashboard");
                    } else {
                        log.debug("Record creation failed for " + runName + " failed with status code - " + response.getStatusCode());
                    }

                    break;
                case StandardEventsTypes.SCENARIO_COMPLETE_EVENT:
                    ScenarioCompleteEvent scenarioCompleteEvent = (ScenarioCompleteEvent) event;
                    ScenarioResult scenarioResult = scenarioCompleteEvent.getResult();
                    response = RestAssured.given(requestSpecBuilder.build()).param("name", runName).param("passed", scenarioResult.isPassed()).param("count", 1).get(subUrlByNameUpdateIterations);
                    if (response.getStatusCode() == 200) {
                        log.debug("Iterations updated successfully");
                    } else {
                        log.debug("Failed to update the iterations with status code +" + response.getStatusCode());
                    }
                    break;
                case StandardEventsTypes.TEST_INCIDENT_OCCURRENCE_EVENT:
                    response = RestAssured.given(requestSpecBuilder.build()).param("name", runName).param("count", 1).get(subUrlByNameUpdateIncidents);
                    if (response.getStatusCode() == 200) {
                        log.debug("Incident updated successfully");
                    } else {
                        log.debug("Failed to update the incident with status code +" + response.getStatusCode());
                    }
                    break;
                case StandardEventsTypes.FEATURE_COMPLETE_EVENT:
                    response = RestAssured.given(requestSpecBuilder.build()).param("name", runName).get(subUrlByNameUpdateStatus);
                    if (response.getStatusCode() == 200) {
                        log.debug("Status updated successfully");
                    } else {
                        log.debug("Failed to update the run status with status code +" + response.getStatusCode());
                    }
                    break;

            }
        } catch (Throwable t) {
            log.error("Exception during event processing: ", t);
        }
    }

}
