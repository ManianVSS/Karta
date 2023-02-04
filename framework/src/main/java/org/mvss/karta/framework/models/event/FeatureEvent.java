package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.models.test.TestFeature;
import org.mvss.karta.framework.utils.ParserUtils;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public abstract class FeatureEvent extends Event {
    private static final long serialVersionUID = 1L;

    public FeatureEvent(Event event) {
        super(event);
        parameters.put(Constants.FEATURE, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.FEATURE), TestFeature.class));
        parameters.put(Constants.FEATURE_NAME, ParserUtils.convertValue(DataFormat.JSON, parameters.get(Constants.FEATURE_NAME), String.class));
    }

    public FeatureEvent(String eventName, String runName, String featureName) {
        super(eventName, runName);
        this.parameters.put(Constants.FEATURE, TestFeature.builder().name(featureName).build());
        this.parameters.put(Constants.FEATURE_NAME, featureName);
    }

    public FeatureEvent(String eventName, String runName, TestFeature feature) {
        super(eventName, runName);
        this.parameters.put(Constants.FEATURE, feature);
        this.parameters.put(Constants.FEATURE_NAME, feature.getName());
    }

    @JsonIgnore
    public TestFeature getFeature() {
        return (TestFeature) parameters.get(Constants.FEATURE);
    }

    @JsonIgnore
    public String getFeatureName() {
        return parameters.get(Constants.FEATURE_NAME).toString();
    }
}
