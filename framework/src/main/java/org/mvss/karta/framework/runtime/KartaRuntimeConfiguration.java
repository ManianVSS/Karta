package org.mvss.karta.framework.runtime;

import java.io.Serializable;
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

   private String                                    defaultFeatureSourceParserPlugin;

   private String                                    defaultStepRunnerPlugin;

   @Builder.Default
   private HashSet<String>                           defaultTestDataSourcePlugins = new HashSet<String>();

   // private HashMap<String, HashMap<String, Serializable>> pluginConfiguration;

   @Builder.Default
   private HashSet<String>                           propertyFiles                = new HashSet<String>();

   @Builder.Default
   private HashSet<String>                           testRepositorydirectories    = new HashSet<String>();

   @Builder.Default
   private HashSet<String>                           testCatalogFiles             = new HashSet<String>();

   @Builder.Default
   private SSLProperties                             sSLProperties                = new SSLProperties();

   @Builder.Default
   private HashMap<String, KartaMinionConfiguration> minions                      = new HashMap<String, KartaMinionConfiguration>();

}
