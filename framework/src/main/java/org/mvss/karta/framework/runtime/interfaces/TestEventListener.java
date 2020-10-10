package org.mvss.karta.framework.runtime.interfaces;

import org.mvss.karta.framework.runtime.event.Event;

public interface TestEventListener extends Plugin
{
   void processEvent( Event event );
}
