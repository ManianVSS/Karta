package org.mvss.karta.framework.annotations.javatest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to be used in Java feature files to denote methods of a feature class to be called as a scenario setup method before very scenario.
 *
 * @author Manian
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ScenarioSetup {
    /**
     * The name of the scenario setup step
     */
    String value() default "";

    /**
     * The priority of the scenario setup step.
     */
    int sequence() default Integer.MAX_VALUE;
}
