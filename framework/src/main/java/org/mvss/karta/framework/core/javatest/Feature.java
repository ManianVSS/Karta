package org.mvss.karta.framework.core.javatest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( ElementType.TYPE )
@Retention( RetentionPolicy.RUNTIME )
public @interface Feature
{
   public String value();

   public String description() default "";
}
