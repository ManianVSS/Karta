package org.mvss.karta.framework.models.event;

import lombok.*;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.models.test.TestScenario;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class ScenarioStartEvent extends ScenarioEvent {
    private static final long serialVersionUID = 1L;

    public ScenarioStartEvent(Event event) {
        super(event);
    }

    public ScenarioStartEvent(String runName, String featureName, long iterationNumber, TestScenario scenario) {
        super(StandardEventsTypes.SCENARIO_START_EVENT, runName, featureName, iterationNumber, scenario);
    }
}
