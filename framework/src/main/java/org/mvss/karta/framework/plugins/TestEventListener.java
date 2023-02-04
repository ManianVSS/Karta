package org.mvss.karta.framework.plugins;

import org.mvss.karta.framework.models.event.Event;

public interface TestEventListener extends Plugin {
    void processEvent(Event event);
}
