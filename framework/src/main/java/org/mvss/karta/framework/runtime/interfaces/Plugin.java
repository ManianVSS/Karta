package org.mvss.karta.framework.runtime.interfaces;

import java.io.Serializable;
import java.util.HashMap;

public interface Plugin extends AutoCloseable
{
   String getPluginName();

   boolean initialize( HashMap<String, HashMap<String, Serializable>> properties ) throws Throwable;

   @Override
   default void close()
   {

   }
}
