package org.mvss.karta.framework.runtime.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mvss.karta.framework.runtime.Constants;

@Target( ElementType.FIELD )
@Retention( RetentionPolicy.RUNTIME )
public @interface PropertyMapping
{
   public String value() default Constants.EMPTY_STRING;

   public String group() default Constants.KARTA;

   public Class<?> type() default Object.class;
}
