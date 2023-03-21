package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.enums.DataFormat;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.models.test.PreparedChaosAction;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class ScenarioChaosActionStartEvent extends ScenarioEvent {
    private static final long serialVersionUID = 1L;

    public ScenarioChaosActionStartEvent(Event event) {
        super(event);
        parameters.put(Constants.CHAOS_ACTION,
                ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.CHAOS_ACTION), PreparedChaosAction.class));
    }

    public ScenarioChaosActionStartEvent(String runName, String featureName, long iterationNumber, String scenarioName,
                                         PreparedChaosAction chaosAction) {
        super(StandardEventsTypes.SCENARIO_CHAOS_ACTION_START_EVENT, runName, featureName, iterationNumber, scenarioName);
        this.parameters.put(Constants.CHAOS_ACTION, chaosAction);
    }

    @JsonIgnore
    public PreparedChaosAction getPreparedChaosAction() {
        return (PreparedChaosAction) parameters.get(Constants.CHAOS_ACTION);
    }
}
