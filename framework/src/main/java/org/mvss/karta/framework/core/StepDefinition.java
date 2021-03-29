package org.mvss.karta.framework.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mvss.karta.framework.enums.StepOutputType;

/**
 * This annotation is used by Kriya plug-in to map step definition method call to step identifier
 * 
 * @author Manian
 */
@Target( ElementType.METHOD )
@Retention( RetentionPolicy.RUNTIME )
public @interface StepDefinition
{
   public String value();

   public StepOutputType outputType() default StepOutputType.AUTO_RESOLVE;

   public String outputName() default "";
}
