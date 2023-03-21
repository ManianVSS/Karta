package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.enums.DataFormat;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.framework.core.StandardEventsTypes;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class JavaScenarioSetupStartEvent extends ScenarioEvent {
    private static final long serialVersionUID = 1L;

    public JavaScenarioSetupStartEvent(Event event) {
        super(event);
        parameters.put(Constants.STEP_IDENTIFIER,
                ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.STEP_IDENTIFIER), String.class));
    }

    public JavaScenarioSetupStartEvent(String runName, String featureName, long iterationNumber, String scenarioName, String stepIdentifier) {
        super(StandardEventsTypes.JAVA_SCENARIO_SETUP_START_EVENT, runName, featureName, iterationNumber, scenarioName);
        this.parameters.put(Constants.STEP_IDENTIFIER, stepIdentifier);
    }

    @JsonIgnore
    public String getStepIdentifier() {
        return parameters.get(Constants.STEP_IDENTIFIER).toString();
    }
}
