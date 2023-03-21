package org.mvss.karta.dependencyinjection.interfaces;

import java.lang.reflect.Method;

/**
 * This interface/Functional interface is a consumer function for a class and a static method belonging to the class.
 *
 * @author Manian
 */
@FunctionalInterface
public interface ClassMethodConsumer {
    void accept(Class<?> classToWorkWith, Method method);
}
