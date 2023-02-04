package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.models.test.TestJob;
import org.mvss.karta.framework.models.test.TestStep;
import org.mvss.karta.framework.utils.ParserUtils;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class JobStepStartEvent extends JobEvent {
    private static final long serialVersionUID = 1L;

    public JobStepStartEvent(Event event) {
        super(event);
        parameters.put(Constants.STEP, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.STEP), TestStep.class));
    }

    public JobStepStartEvent(String runName, String featureName, TestJob job, long iterationNumber, TestStep step) {
        super(StandardEventsTypes.JOB_STEP_START_EVENT, runName, featureName, job, iterationNumber);
        this.parameters.put(Constants.STEP, step);
    }

    @JsonIgnore
    public TestStep getStep() {
        return (TestStep) parameters.get(Constants.STEP);
    }
}
