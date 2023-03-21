package org.mvss.karta.dependencyinjection.interfaces;

public interface DependencyInjector extends AutoCloseable {
    /**
     * This interface method should inject dependencies into a class statics
     *
     * @param classToInject The class into which static dependencies should be injected into.
     */
    void injectIntoClass(Class<?> classToInject);

    /**
     * This interface method should inject dependencies into an object
     *
     * @param objectToInject The object into which static dependencies should be injected into.
     */
    void injectIntoObject(Object objectToInject);

}
