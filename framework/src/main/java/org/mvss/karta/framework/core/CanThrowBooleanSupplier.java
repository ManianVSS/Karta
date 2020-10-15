package org.mvss.karta.framework.core;

@FunctionalInterface
public interface CanThrowBooleanSupplier
{
   public boolean evaluate() throws Throwable;
}
