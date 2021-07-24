package org.mvss.karta.framework.core;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.mvss.karta.framework.enums.ContextType;
import org.mvss.karta.framework.runtime.Constants;

/**
 * This annotation is used to auto wire beans to be injected by BeanRegistry.
 * Pass the bean name in the value. Defaulted to bean class name by BeanRegistry if empty.
 * Bean definitions are annotated with {@link KartaBean} annotation.
 * 
 * @see org.mvss.karta.framework.runtime.BeanRegistry
 * @see org.mvss.karta.framework.core.KartaBean
 * @author Manian
 */
@Retention( RUNTIME )
@Target( FIELD )
public @interface KartaAutoWired
{
   public String value() default Constants.EMPTY_STRING;

   // Alias for value
   public String name() default Constants.EMPTY_STRING;

   public ContextType contextType() default ContextType.GLOBAL;

   public String contextName() default Constants.EMPTY_STRING;
}
