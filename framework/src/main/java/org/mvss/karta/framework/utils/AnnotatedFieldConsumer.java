package org.mvss.karta.framework.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

@FunctionalInterface
public interface AnnotatedFieldConsumer
{
   void accept( Class<?> type, Field field, Annotation annotationObject );
}
