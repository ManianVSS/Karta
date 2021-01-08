package org.mvss.karta.framework.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used by Kriya plug-in to allow definition of the before run life cycle hook. </br>
 * The value are the regular expression tag patterns on run tags to match.</br>
 * 
 * @author Manian
 */
@Target( ElementType.METHOD )
@Retention( RetentionPolicy.RUNTIME )
public @interface BeforeRun
{
   // Tags
   public String[] value();
}