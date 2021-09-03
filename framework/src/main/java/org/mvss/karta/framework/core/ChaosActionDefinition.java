package org.mvss.karta.framework.core;

import org.mvss.karta.framework.enums.StepOutputType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used by Kriya plug-in to map chaos action definition method call to chaos action name
 *
 * @author Manian
 */
@Target( ElementType.METHOD )
@Retention( RetentionPolicy.RUNTIME )
public @interface ChaosActionDefinition
{
   String value();

   StepOutputType outputType() default StepOutputType.AUTO_RESOLVE;

   String outputName() default "";
}
