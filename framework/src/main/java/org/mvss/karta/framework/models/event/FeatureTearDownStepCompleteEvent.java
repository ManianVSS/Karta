package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.models.test.TestFeature;
import org.mvss.karta.framework.models.test.TestStep;
import org.mvss.karta.framework.utils.ParserUtils;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class FeatureTearDownStepCompleteEvent extends FeatureEvent {
    private static final long serialVersionUID = 1L;

    public FeatureTearDownStepCompleteEvent(Event event) {
        super(event);
        parameters.put(Constants.STEP, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.STEP), TestStep.class));
        parameters.put(Constants.RESULT, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.RESULT), StepResult.class));
    }

    public FeatureTearDownStepCompleteEvent(String runName, TestFeature feature, TestStep step, StepResult result) {
        super(StandardEventsTypes.FEATURE_TEARDOWN_STEP_COMPLETE_EVENT, runName, feature);
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
