package org.mvss.karta.framework.annotations;

import org.mvss.karta.framework.enums.StepOutputType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used by Kriya plug-in to map step definition method call to step identifier
 *
 * @author Manian
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface StepDefinition {
    String value();

    StepOutputType outputType() default StepOutputType.AUTO_RESOLVE;

    String outputName() default "";
}
