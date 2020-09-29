package org.mvss.karta.framework.runtime.interfaces;

import java.io.Serializable;
import java.util.HashMap;

public interface Plugin
{
   String getPluginName();

   boolean initialize( HashMap<String, HashMap<String, Serializable>> runProperties ) throws Throwable;
}
