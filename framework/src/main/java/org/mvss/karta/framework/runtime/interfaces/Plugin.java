package org.mvss.karta.framework.runtime.interfaces;

public interface Plugin extends AutoCloseable
{
   String getPluginName();

   // Plugin initialize approach is changed to using @Initializer

   @Override
   default void close()
   {

   }
}
