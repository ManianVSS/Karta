package org.mvss.karta.dependencyinjection.annotations;

import org.mvss.karta.dependencyinjection.BeanRegistry;
import org.mvss.karta.dependencyinjection.Constants;
import org.mvss.karta.dependencyinjection.enums.ContextType;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation is used to auto wire beans to be injected by BeanRegistry.
 * Pass the bean name in the value. Defaulted to bean class name by BeanRegistry if empty.
 * Bean definitions are annotated with {@link KartaBean} annotation.
 *
 * @author Manian
 * @see BeanRegistry
 * @see KartaBean
 */
@Retention(RUNTIME)
@Target(FIELD)
public @interface KartaAutoWired {
    String value() default Constants.EMPTY_STRING;

    /**
     * Alias for value
     */
    String name() default Constants.EMPTY_STRING;

    ContextType contextType() default ContextType.GLOBAL;

    String contextName() default Constants.EMPTY_STRING;
}
