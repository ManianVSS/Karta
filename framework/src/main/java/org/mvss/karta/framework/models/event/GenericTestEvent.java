package org.mvss.karta.framework.models.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.framework.core.StandardEventsTypes;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class GenericTestEvent extends Event {
    private static final long serialVersionUID = 1L;

    public GenericTestEvent(Event event) {
        super(event);
    }

    public GenericTestEvent(String runName, Serializable eventData) {
        super(StandardEventsTypes.GENERIC_TEST_EVENT, runName);
        this.parameters.put("eventData", eventData);
    }

    @JsonIgnore
    public Serializable getEventData() {
        return parameters.get("eventData");
    }
}
