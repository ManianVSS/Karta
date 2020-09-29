package org.mvss.karta.configuration;

import java.io.Serializable;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KartaBaseConfiguration implements Serializable
{
   /**
    * 
    */
   private static final long       serialVersionUID = 1L;

   private ArrayList<PluginConfig> pluginConfigs;

   private String                  pluginsDirectory;

}
