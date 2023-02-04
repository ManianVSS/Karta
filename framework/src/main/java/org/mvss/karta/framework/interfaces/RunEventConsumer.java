package org.mvss.karta.framework.interfaces;

import org.mvss.karta.framework.models.run.RunTarget;

@FunctionalInterface
@SuppressWarnings("unused")
public interface RunEventConsumer {
    void consume(String runName, RunTarget runTarget);
}
