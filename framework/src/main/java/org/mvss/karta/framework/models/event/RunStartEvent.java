package org.mvss.karta.framework.models.event;

import lombok.*;
import org.mvss.karta.framework.core.StandardEventsTypes;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class RunStartEvent extends Event {
    private static final long serialVersionUID = 1L;

    public RunStartEvent(Event event) {
        super(event);
    }

    public RunStartEvent(String runName) {
        super(StandardEventsTypes.RUN_START_EVENT, runName);
    }
}
