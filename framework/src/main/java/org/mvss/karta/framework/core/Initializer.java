package org.mvss.karta.framework.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.mvss.karta.framework.runtime.KartaRuntime;

/**
 * This annotation is to mark methods to be called for initializing the class after loading properties and beans. </br>
 * 
 * @see KartaRuntime#initializeObject(Object)
 * @see KartaRuntime#initializeClass(Class)
 * @author Manian
 */
@Target( ElementType.METHOD )
@Retention( RetentionPolicy.RUNTIME )
public @interface Initializer
{

}
