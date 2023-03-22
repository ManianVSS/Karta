package org.mvss.karta.dependencyinjection.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * The functional interface to define processing of a class field annotated with an annotation type.
 *
 * @author Manian
 */
@FunctionalInterface
public interface AnnotatedFieldConsumer {
    void accept(Class<?> type, Field field, Annotation annotationObject);
}
