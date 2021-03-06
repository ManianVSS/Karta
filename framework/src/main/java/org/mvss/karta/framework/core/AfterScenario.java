package org.mvss.karta.framework.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mvss.karta.framework.runtime.Constants;

/**
 * This annotation is used by Kriya plug-in to allow definition of the after scenario life cycle hook. </br>
 * The value are the regular expression tag patterns to match test(feature) with tag.</br>
 * This is typically used to cleanup and close TestExecutionContext specific beans and resources.</br>
 * 
 * @author Manian
 */
@Target( ElementType.METHOD )
@Retention( RetentionPolicy.RUNTIME )
public @interface AfterScenario
{
   // Tags
   public String[] value() default Constants.REGEX_ALL_STRING;
}
