package org.mvss.karta.framework.core;

import org.mvss.karta.framework.models.result.ScenarioResult;
import org.mvss.karta.framework.models.test.TestIncident;

import java.util.Date;

/**
 * Utility class for scenario results
 *
 * @author Manian
 */
public class StandardScenarioResults {
    public final static ScenarioResult passed = ScenarioResult.builder().successful(true).build();
    public final static ScenarioResult failed = ScenarioResult.builder().successful(false).build();

    public static ScenarioResult error(String message) {
        return error(TestIncident.builder().message(message).build());
    }

    public static ScenarioResult error(Throwable t) {
        return error(TestIncident.builder().message(t.getMessage()).thrownCause(t).build());
    }

    public static ScenarioResult error(TestIncident incident) {
        ScenarioResult result = ScenarioResult.builder().error(true).build();
        result.setEndTime(new Date());
        result.getIncidents().add(incident);
        return result;
    }

    public static ScenarioResult failure(Throwable t) {
        return failure(TestIncident.builder().thrownCause(t).build());
    }

    public static ScenarioResult failure(TestIncident incident) {
        ScenarioResult result = ScenarioResult.builder().successful(false).build();
        result.setEndTime(new Date());
        result.getIncidents().add(incident);
        return result;
    }
}
