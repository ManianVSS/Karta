package org.mvss.karta.dependencyinjection.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to mark classes whose static fields needs to be loaded with properties. </br>
 *
 * @author Manian
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoadConfiguration {

}
