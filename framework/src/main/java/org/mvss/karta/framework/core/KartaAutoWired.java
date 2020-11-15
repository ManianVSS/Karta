package org.mvss.karta.framework.core;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.mvss.karta.framework.runtime.Constants;

@Retention( RUNTIME )
@Target( FIELD )
public @interface KartaAutoWired
{
   public String value() default Constants.EMPTY_STRING;
}
