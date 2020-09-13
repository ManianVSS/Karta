package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class Plugin<T>
{
   public Class<? extends T>            pluginClass;
   public HashMap<String, Serializable> pluginProperties;
}
