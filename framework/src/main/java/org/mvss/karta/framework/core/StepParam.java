package org.mvss.karta.framework.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to map the step definition method parameters to a test data, variable or bean.
 * 
 * @author Manian
 */
@Target( ElementType.PARAMETER )
@Retention( RetentionPolicy.RUNTIME )
public @interface StepParam
{
   public String value();

   public ParameterMapping mapto() default ParameterMapping.TESTDATA;
}
