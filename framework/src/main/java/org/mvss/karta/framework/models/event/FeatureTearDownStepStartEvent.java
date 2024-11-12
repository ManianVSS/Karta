package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.enums.DataFormat;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.models.test.PreparedStep;
import org.mvss.karta.framework.models.test.TestFeature;

import java.io.Serial;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class FeatureTearDownStepStartEvent extends FeatureEvent {
    @Serial
    private static final long serialVersionUID = 1L;

    public FeatureTearDownStepStartEvent(Event event) {
        super(event);
        parameters.put(Constants.STEP, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.STEP), PreparedStep.class));
    }

    public FeatureTearDownStepStartEvent(String runName, TestFeature feature, PreparedStep step) {
        super(StandardEventsTypes.FEATURE_TEARDOWN_STEP_START_EVENT, runName, feature);
        this.parameters.put(Constants.STEP, step);
    }

    @JsonIgnore
    public PreparedStep getStep() {
        return (PreparedStep) parameters.get(Constants.STEP);
    }
}
