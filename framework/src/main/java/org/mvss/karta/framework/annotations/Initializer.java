package org.mvss.karta.framework.annotations;

import org.mvss.karta.framework.runtime.KartaRuntime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is to mark methods to be called for initializing the class after loading properties and beans. </br>
 *
 * @author Manian
 * @see KartaRuntime#initializeObject(Object)
 * @see KartaRuntime#initializeClass(Class)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Initializer {

}
