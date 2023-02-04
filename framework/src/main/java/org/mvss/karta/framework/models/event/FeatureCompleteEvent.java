package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.models.result.FeatureResult;
import org.mvss.karta.framework.models.test.TestFeature;
import org.mvss.karta.framework.utils.ParserUtils;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class FeatureCompleteEvent extends FeatureEvent {
    private static final long serialVersionUID = 1L;

    public FeatureCompleteEvent(Event event) {
        super(event);
        parameters.put(Constants.RESULT, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.RESULT), FeatureResult.class));
    }

    public FeatureCompleteEvent(String runName, TestFeature feature, FeatureResult result) {
        super(StandardEventsTypes.FEATURE_COMPLETE_EVENT, runName, feature);
        this.parameters.put(Constants.RESULT, result);
    }

    @JsonIgnore
    public FeatureResult getResult() {
        return (FeatureResult) parameters.get(Constants.RESULT);
    }
}
