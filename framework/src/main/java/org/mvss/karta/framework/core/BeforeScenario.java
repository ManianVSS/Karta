package org.mvss.karta.framework.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used by Kriya plug-in to allow definition of the before scenario life cycle hook. </br>
 * The value are the regular expression tag patterns to match test(feature) with tag.</br>
 * This is typically used to setup and initialize TestExecutionContext specific beans and resources.</br>
 * 
 * @author Manian
 */
@Target( ElementType.METHOD )
@Retention( RetentionPolicy.RUNTIME )
public @interface BeforeScenario
{
   // Tags
   public String[] value();
}
