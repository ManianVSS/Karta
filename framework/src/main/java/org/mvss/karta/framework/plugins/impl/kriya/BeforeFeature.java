package org.mvss.karta.framework.plugins.impl.kriya;

import org.mvss.karta.Constants;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used by Kriya plug-in to allow definition of the before feature life cycle hook. </br>
 * The value are the regular expression tag patterns to match test(feature) with tag.</br>
 *
 * @author Manian
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BeforeFeature {
    /**
     * Tag regex patterns to match
     */
    String[] value() default Constants.REGEX_ALL_STRING;
}
