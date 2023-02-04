package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.models.test.PreparedStep;
import org.mvss.karta.framework.utils.ParserUtils;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class ScenarioTearDownStepStartEvent extends ScenarioEvent {
    private static final long serialVersionUID = 1L;

    public ScenarioTearDownStepStartEvent(Event event) {
        super(event);
        parameters.put(Constants.STEP, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.STEP), PreparedStep.class));
    }

    public ScenarioTearDownStepStartEvent(String runName, String featureName, long iterationNumber, String scenarioName, PreparedStep step) {
        super(StandardEventsTypes.SCENARIO_TEARDOWN_STEP_START_EVENT, runName, featureName, iterationNumber, scenarioName);
        this.parameters.put(Constants.STEP, step);
    }

    @JsonIgnore
    public PreparedStep getStep() {
        return (PreparedStep) parameters.get(Constants.STEP);
    }
}
