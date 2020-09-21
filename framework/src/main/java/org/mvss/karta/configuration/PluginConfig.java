package org.mvss.karta.configuration;

import java.io.Serializable;
import java.util.HashSet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginConfig implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            pluginName;
   private HashSet<String>   pluginTypes;
   private String            className;
}
