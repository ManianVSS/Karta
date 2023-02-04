package org.mvss.karta.framework.models.event;

import lombok.*;
import org.mvss.karta.framework.core.StandardEventsTypes;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class JavaFeatureStartEvent extends FeatureEvent {
    private static final long serialVersionUID = 1L;

    public JavaFeatureStartEvent(Event event) {
        super(event);
    }

    public JavaFeatureStartEvent(String runName, String featureName) {
        super(StandardEventsTypes.JAVA_FEATURE_START_EVENT, runName, featureName);
    }
}
