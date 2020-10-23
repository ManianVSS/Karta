package org.mvss.karta.framework.runtime.interfaces;

public interface Plugin extends AutoCloseable
{
   String getPluginName();

   boolean initialize() throws Throwable;

   @Override
   default void close()
   {

   }
}
