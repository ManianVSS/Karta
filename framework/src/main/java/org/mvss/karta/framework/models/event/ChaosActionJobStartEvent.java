package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.enums.DataFormat;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.models.chaos.ChaosAction;
import org.mvss.karta.framework.models.test.TestJob;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class ChaosActionJobStartEvent extends JobEvent {
    private static final long serialVersionUID = 1L;

    public ChaosActionJobStartEvent(Event event) {
        super(event);
        parameters.put(Constants.CHAOS_ACTION,
                ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.CHAOS_ACTION), ChaosAction.class));
    }

    public ChaosActionJobStartEvent(String runName, String featureName, TestJob job, long iterationNumber, ChaosAction chaosAction) {
        super(StandardEventsTypes.CHAOS_ACTION_JOB_START_EVENT, runName, featureName, job, iterationNumber);
        this.parameters.put(Constants.CHAOS_ACTION, chaosAction);
    }

    @JsonIgnore
    public ChaosAction getChaosAction() {
        return (ChaosAction) parameters.get(Constants.CHAOS_ACTION);
    }
}
