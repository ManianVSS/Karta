package org.mvss.karta.framework.core.javatest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to be used in Java feature files to denote methods of a feature class to be called as a scenario tear-down method after very scenario.
 * 
 * @author Manian
 */
@Target( {ElementType.METHOD} )
@Retention( RetentionPolicy.RUNTIME )
public @interface ScenarioTearDown
{
   /**
    * The name of the scenario tear-down step
    * 
    * @return
    */
   public String value() default "";

   /**
    * The priority of the scenario tear-down step.
    * 
    * @return
    */
   public int sequence() default Integer.MAX_VALUE;
}
