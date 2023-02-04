package org.mvss.karta.framework.annotations;

import org.mvss.karta.Constants;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation is used for bean definitions when initializing Runtime
 *
 * @author Manian
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface KartaBean {
    String value() default Constants.EMPTY_STRING;

    /**
     * Alias for value
     */
    String name() default Constants.EMPTY_STRING;
}
