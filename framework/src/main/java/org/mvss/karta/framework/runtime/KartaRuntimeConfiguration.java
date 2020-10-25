package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.mvss.karta.framework.minions.KartaMinionConfiguration;
import org.mvss.karta.framework.utils.PropertyUtils;
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

   @Builder.Default
   private ArrayList<String>                         pluginsDirectories           = new ArrayList<String>();

   private String                                    defaultFeatureSourceParserPlugin;

   private String                                    defaultStepRunnerPlugin;

   @Builder.Default
   private HashSet<String>                           defaultTestDataSourcePlugins = new HashSet<String>();

   private HashSet<String>                           enabledPlugins;

   @Builder.Default
   private ArrayList<String>                         propertyFiles                = new ArrayList<String>();

   private SSLProperties                             sslProperties;

   private String                                    nodeName;

   @Builder.Default
   private HashMap<String, KartaMinionConfiguration> nodes                        = new HashMap<String, KartaMinionConfiguration>();

   // @Builder.Default
   // private Boolean enableMinions = true;

   // @Builder.Default
   // private HashMap<String, KartaMinionConfiguration> minions = new HashMap<String, KartaMinionConfiguration>();

   public synchronized void expandSystemAndEnvProperties()
   {
      PropertyUtils.expandEnvVars( pluginsDirectories );
      defaultFeatureSourceParserPlugin = PropertyUtils.expandEnvVars( defaultFeatureSourceParserPlugin );
      defaultStepRunnerPlugin = PropertyUtils.expandEnvVars( defaultStepRunnerPlugin );
      PropertyUtils.expandEnvVars( defaultTestDataSourcePlugins );
      PropertyUtils.expandEnvVars( enabledPlugins );
      PropertyUtils.expandEnvVars( propertyFiles );
      sslProperties.expandSystemAndEnvProperties();
      nodeName = PropertyUtils.expandEnvVars( nodeName );
   }
}
