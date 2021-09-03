package org.mvss.karta.framework.core;

/**
 * A boolean supplier functional interface which can throw Throwable.</br>
 * This makes it useful if error/exception is to be additionally supplied.</br>
 *
 * @author Manian
 */
@FunctionalInterface
public interface CanThrowBooleanSupplier
{
   boolean evaluate() throws Throwable;
}
