package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.runtime.RunTarget;

@FunctionalInterface
public interface RunEventConsumer
{
   void consume( String runName, RunTarget runTarget );
}
