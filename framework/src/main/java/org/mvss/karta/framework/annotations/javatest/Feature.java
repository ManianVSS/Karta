package org.mvss.karta.framework.annotations.javatest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to be used in Java feature files to denote a feature class.
 *
 * @author Manian
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Feature {
    /**
     * The name of the feature
     */
    String value();

    /**
     * The description of the feature
     */
    String description() default "";
}
