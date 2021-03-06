package org.mvss.karta.framework.core.javatest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to be used in Java feature files to denote methods of a feature class to be called for feature setup once.
 * 
 * @author Manian
 */
@Target( {ElementType.METHOD} )
@Retention( RetentionPolicy.RUNTIME )
public @interface FeatureSetup
{
   /**
    * Name of the feature setup (considered as a test step)
    * 
    * @return
    */
   public String value() default "";

   /**
    * The priority of the feature setup step.
    * 
    * @return
    */
   public int sequence() default Integer.MAX_VALUE;
}
