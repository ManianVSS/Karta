package org.mvss.karta.framework.annotations.javatest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to be used in Java feature files to denote methods of a feature class to be called as a scenario.
 *
 * @author Manian
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Scenario {
    /**
     * The name of the scenario.
     */
    String value();

    /**
     * The priority of the scenario.
     */
    int sequence() default Integer.MAX_VALUE;

    /**
     * The probability that this test scenario should be run for an iteration of the feature run.
     * This value is used when the run configuration for the feature is to run exclusive scenario per iteration or probability based selection of scenarios for feature run iteration.
     */
    float probability() default 1.0f;
}
