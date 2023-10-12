package org.mvss.karta.dependencyinjection.annotations;

import org.mvss.karta.dependencyinjection.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyMapping {
    /**
     * The name of the property
     */
    String value() default Constants.EMPTY_STRING;

    /**
     * Alias for default value
     **/
    String name() default Constants.EMPTY_STRING;

    /**
     * The name of the property group
     */
    String group() default Constants.KARTA;

    /**
     * The json path to parse property from
     */
    String jsonPath() default Constants.EMPTY_STRING;

    /**
     * The class type to map the property value.
     */
    Class<?> type() default Object.class;
}
