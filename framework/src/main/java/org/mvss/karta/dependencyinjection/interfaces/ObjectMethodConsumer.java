package org.mvss.karta.dependencyinjection.interfaces;

import java.lang.reflect.Method;

/**
 * This interface/Functional interface is a consumer function for an object and a method belonging to the object's class.
 *
 * @author Manian
 */
@FunctionalInterface
public interface ObjectMethodConsumer {
    void accept(Object object, Method method);
}
