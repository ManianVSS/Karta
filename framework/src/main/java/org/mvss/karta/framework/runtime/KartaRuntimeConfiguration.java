package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.mvss.karta.framework.minions.KartaMinionConfiguration;
import org.mvss.karta.framework.utils.SSLProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KartaRuntimeConfiguration implements Serializable
{
   /**
    * 
    */
   private static final long                         serialVersionUID             = 1L;

   private String                                    pluginsDirectory;

   private String                                    defaultFeatureSourceParserPlugin;

   private String                                    defaultStepRunnerPlugin;

   @Builder.Default
   private HashSet<String>                           defaultTestDataSourcePlugins = new HashSet<String>();

   // private HashMap<String, HashMap<String, Serializable>> pluginConfiguration;

   private HashSet<String>                           enabledPlugins;

   @Builder.Default
   private ArrayList<String>                         propertyFiles                = new ArrayList<String>();

   @Builder.Default
   private ArrayList<String>                         testRepositorydirectories    = new ArrayList<String>();

   @Builder.Default
   private ArrayList<String>                         testCatalogFiles             = new ArrayList<String>();

   private SSLProperties                             sslProperties;

   private String                                    nodeName;

   // @Builder.Default
   // private Boolean enableMinions = true;

   @Builder.Default
   private HashMap<String, KartaMinionConfiguration> nodes                        = new HashMap<String, KartaMinionConfiguration>();

   // @Builder.Default
   // private HashMap<String, KartaMinionConfiguration> minions = new HashMap<String, KartaMinionConfiguration>();

}
