package org.mvss.karta.framework.plugins;

public interface Plugin extends AutoCloseable {
    String getPluginName();

    // Plugin initialize approach is changed to using @Initializer

    @Override
    default void close() {

    }
}
