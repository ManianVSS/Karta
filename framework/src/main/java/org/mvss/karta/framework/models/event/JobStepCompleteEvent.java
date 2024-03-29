package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.enums.DataFormat;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.models.test.TestJob;
import org.mvss.karta.framework.models.test.TestStep;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class JobStepCompleteEvent extends JobEvent {
    private static final long serialVersionUID = 1L;

    public JobStepCompleteEvent(Event event) {
        super(event);
        parameters.put(Constants.STEP, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.STEP), TestStep.class));
        parameters.put(Constants.RESULT, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.RESULT), StepResult.class));
    }

    public JobStepCompleteEvent(String runName, String featureName, TestJob job, long iterationNumber, TestStep step, StepResult result) {
        super(StandardEventsTypes.JOB_STEP_COMPLETE_EVENT, runName, featureName, job, iterationNumber);
        this.parameters.put(Constants.STEP, step);
        this.parameters.put(Constants.RESULT, result);
    }

    @JsonIgnore
    public TestStep getStep() {
        return (TestStep) parameters.get(Constants.STEP);
    }

    @JsonIgnore
    public StepResult getResult() {
        return (StepResult) parameters.get(Constants.RESULT);
    }
}
