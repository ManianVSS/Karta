package org.mvss.karta.framework.core;

import org.mvss.karta.framework.runtime.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used by Kriya plug-in to allow definition of the after feature life cycle hook. </br>
 * The value are the regular expression tag patterns to match test(feature) with tag.</br>
 *
 * @author Manian
 */
@Target( ElementType.METHOD )
@Retention( RetentionPolicy.RUNTIME )
public @interface AfterFeature
{
   /**
    * Tag regex patterns to match
    */
   String[] value() default Constants.REGEX_ALL_STRING;
}
