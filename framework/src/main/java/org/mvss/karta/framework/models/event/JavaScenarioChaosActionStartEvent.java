package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.models.chaos.ChaosAction;
import org.mvss.karta.framework.utils.ParserUtils;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class JavaScenarioChaosActionStartEvent extends ScenarioEvent {
    private static final long serialVersionUID = 1L;

    public JavaScenarioChaosActionStartEvent(Event event) {
        super(event);
        parameters.put(Constants.CHAOS_ACTION,
                ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.CHAOS_ACTION), ChaosAction.class));
    }

    public JavaScenarioChaosActionStartEvent(String runName, String featureName, long iterationNumber, String scenarioName, ChaosAction chaosAction) {
        super(StandardEventsTypes.JAVA_SCENARIO_CHAOS_ACTION_START_EVENT, runName, featureName, iterationNumber, scenarioName);
        this.parameters.put(Constants.CHAOS_ACTION, chaosAction);
    }

    @JsonIgnore
    public ChaosAction getChaosAction() {
        return (ChaosAction) parameters.get(Constants.CHAOS_ACTION);
    }
}
