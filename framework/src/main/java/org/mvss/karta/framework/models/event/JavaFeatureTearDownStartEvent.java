package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.utils.ParserUtils;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class JavaFeatureTearDownStartEvent extends FeatureEvent {
    private static final long serialVersionUID = 1L;

    public JavaFeatureTearDownStartEvent(Event event) {
        super(event);
        parameters.put(Constants.STEP_IDENTIFIER,
                ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.STEP_IDENTIFIER), String.class));
    }

    public JavaFeatureTearDownStartEvent(String runName, String featureName, String stepIdentifier) {
        super(StandardEventsTypes.JAVA_FEATURE_TEARDOWN_START_EVENT, runName, featureName);
        this.parameters.put(Constants.STEP_IDENTIFIER, stepIdentifier);
    }

    @JsonIgnore
    public String getStepIdentifier() {
        return parameters.get(Constants.STEP_IDENTIFIER).toString();
    }
}
