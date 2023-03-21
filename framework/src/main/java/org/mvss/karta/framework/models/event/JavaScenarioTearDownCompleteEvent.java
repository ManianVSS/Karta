package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.enums.DataFormat;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.models.result.StepResult;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class JavaScenarioTearDownCompleteEvent extends ScenarioEvent {
    private static final long serialVersionUID = 1L;

    public JavaScenarioTearDownCompleteEvent(Event event) {
        super(event);
        parameters.put(Constants.STEP_IDENTIFIER,
                ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.STEP_IDENTIFIER), String.class));
        parameters.put(Constants.RESULT, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.RESULT), StepResult.class));
    }

    public JavaScenarioTearDownCompleteEvent(String runName, String featureName, long iterationNumber, String scenarioName, String stepIdentifier,
                                             StepResult result) {
        super(StandardEventsTypes.JAVA_SCENARIO_TEARDOWN_COMPLETE_EVENT, runName, featureName, iterationNumber, scenarioName);
        this.parameters.put(Constants.STEP_IDENTIFIER, stepIdentifier);
        this.parameters.put(Constants.RESULT, result);
    }

    @JsonIgnore
    public String getStepIdentifier() {
        return parameters.get(Constants.STEP_IDENTIFIER).toString();
    }

    @JsonIgnore
    public StepResult getResult() {
        return (StepResult) parameters.get(Constants.RESULT);
    }
}
