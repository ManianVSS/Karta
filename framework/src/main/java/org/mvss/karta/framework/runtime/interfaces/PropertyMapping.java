package org.mvss.karta.framework.runtime.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( ElementType.FIELD )
@Retention( RetentionPolicy.RUNTIME )
public @interface PropertyMapping
{
   public String propertyName();

   public String group() default "Karta";

   public Class<?> type() default Object.class;
}
