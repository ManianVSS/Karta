package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.enums.DataFormat;
import org.mvss.karta.dependencyinjection.utils.ParserUtils;
import org.mvss.karta.framework.core.StandardEventsTypes;
import org.mvss.karta.framework.models.result.FeatureResult;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class JavaFeatureCompleteEvent extends FeatureEvent {
    private static final long serialVersionUID = 1L;

    public JavaFeatureCompleteEvent(Event event) {
        super(event);
        parameters.put(Constants.RESULT, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.RESULT), FeatureResult.class));
    }

    public JavaFeatureCompleteEvent(String runName, String featureName, FeatureResult result) {
        super(StandardEventsTypes.JAVA_FEATURE_COMPLETE_EVENT, runName, featureName);
        this.parameters.put(Constants.RESULT, result);
    }

    @JsonIgnore
    public FeatureResult getResult() {
        return (FeatureResult) parameters.get(Constants.RESULT);
    }
}
