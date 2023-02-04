package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.models.test.TestFeature;
import org.mvss.karta.framework.models.test.TestStep;
import org.mvss.karta.framework.utils.ParserUtils;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class FeatureTearDownStepStartEvent extends FeatureEvent {
    private static final long serialVersionUID = 1L;

    public FeatureTearDownStepStartEvent(Event event) {
        super(event);
        parameters.put(Constants.STEP, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.STEP), TestStep.class));
    }

    public FeatureTearDownStepStartEvent(String runName, TestFeature feature, TestStep step) {
        super(StandardEventsTypes.FEATURE_TEARDOWN_STEP_START_EVENT, runName, feature);
        this.parameters.put(Constants.STEP, step);
    }

    @JsonIgnore
    public TestStep getStep() {
        return (TestStep) parameters.get(Constants.STEP);
    }
}
