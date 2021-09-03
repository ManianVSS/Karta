package org.mvss.karta.framework.core;

import org.mvss.karta.framework.enums.ContextType;
import org.mvss.karta.framework.runtime.Constants;

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
 * @see org.mvss.karta.framework.runtime.BeanRegistry
 * @see org.mvss.karta.framework.core.KartaBean
 */
@Retention( RUNTIME )
@Target( FIELD )
public @interface KartaAutoWired
{
   String value() default Constants.EMPTY_STRING;

   /**
    * Alias for value
    */
   String name() default Constants.EMPTY_STRING;

   ContextType contextType() default ContextType.GLOBAL;

   String contextName() default Constants.EMPTY_STRING;
}
