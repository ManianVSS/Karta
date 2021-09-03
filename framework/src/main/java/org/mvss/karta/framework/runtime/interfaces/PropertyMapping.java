package org.mvss.karta.framework.runtime.interfaces;

import org.mvss.karta.framework.runtime.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( ElementType.FIELD )
@Retention( RetentionPolicy.RUNTIME )
public @interface PropertyMapping
{
   String value() default Constants.EMPTY_STRING;

   String group() default Constants.KARTA;

   Class<?> type() default Object.class;
}
