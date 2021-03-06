package org.mvss.karta.framework.core;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.mvss.karta.framework.runtime.Constants;

/**
 * This annotation is used for bean definitions when initializing Runtime
 * 
 * @author Manian
 */
@Retention( RUNTIME )
@Target( METHOD )
public @interface KartaBean
{
   public String value() default Constants.EMPTY_STRING;
}
