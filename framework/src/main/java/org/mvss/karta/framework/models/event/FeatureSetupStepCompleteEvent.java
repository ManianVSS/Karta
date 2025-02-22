package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.enums.DataFormat;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.models.test.PreparedStep;
import org.mvss.karta.framework.models.test.TestFeature;

import java.io.Serial;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class FeatureSetupStepCompleteEvent extends FeatureEvent {
    @Serial
    private static final long serialVersionUID = 1L;

    public FeatureSetupStepCompleteEvent(Event event) {
        super(event);
        parameters.put(Constants.STEP, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.STEP), PreparedStep.class));
        parameters.put(Constants.RESULT, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.RESULT), StepResult.class));
    }

    public FeatureSetupStepCompleteEvent(String runName, TestFeature feature, PreparedStep step, StepResult result) {
        super(StandardEventsTypes.FEATURE_SETUP_STEP_COMPLETE_EVENT, runName, feature);
        this.parameters.put(Constants.STEP, step);
        this.parameters.put(Constants.RESULT, result);
    }

    @JsonIgnore
    public PreparedStep getStep() {
        return (PreparedStep) parameters.get(Constants.STEP);
    }

    @JsonIgnore
    public StepResult getResult() {
        return (StepResult) parameters.get(Constants.RESULT);
    }
}
