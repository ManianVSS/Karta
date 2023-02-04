package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.models.run.TestExecutionContext;
import org.mvss.karta.framework.models.test.TestIncident;
import org.mvss.karta.framework.utils.ParserUtils;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class TestIncidentOccurrenceEvent extends ScenarioEvent {
    private static final long serialVersionUID = 1L;

    public TestIncidentOccurrenceEvent(Event event) {
        super(event);
        parameters.put(Constants.STEP_IDENTIFIER,
                ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.STEP_IDENTIFIER), String.class));
        parameters.put(Constants.INCIDENT, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.INCIDENT), TestIncident.class));
    }

    public TestIncidentOccurrenceEvent(String runName, String featureName, long iterationNumber, String scenarioName, String stepIdentifier,
                                       TestIncident incident) {
        super(StandardEventsTypes.TEST_INCIDENT_OCCURRENCE_EVENT, runName, featureName, iterationNumber, scenarioName);
        this.parameters.put(Constants.STEP_IDENTIFIER, stepIdentifier);
        this.parameters.put(Constants.INCIDENT, incident);
    }

    public TestIncidentOccurrenceEvent(TestExecutionContext context, TestIncident incident) {
        this(context.getRunName(), context.getFeatureName(), context.getIterationIndex(), context.getScenarioName(), context.getStepIdentifier(),
                incident);
    }

    @JsonIgnore
    public String getStepIdentifier() {
        return parameters.get(Constants.STEP_IDENTIFIER).toString();
    }

    @JsonIgnore
    public TestIncident getIncident() {
        return (TestIncident) parameters.get(Constants.INCIDENT);
    }
}
