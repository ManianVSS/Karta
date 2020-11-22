package org.mvss.karta.configuration;

import java.io.Serializable;
import java.util.ArrayList;
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
public class KartaConfiguration implements Serializable
{
   /**
    * 
    */
   private static final long                   serialVersionUID             = 1L;

   @Builder.Default
   private ArrayList<String>                   pluginsDirectories           = new ArrayList<String>();

   private String                              defaultFeatureSourceParserPlugin;

   private String                              defaultStepRunnerPlugin;

   @Builder.Default
   private HashSet<String>                     defaultTestDataSourcePlugins = new HashSet<String>();

   private HashSet<String>                     enabledPlugins;

   @Builder.Default
   private ArrayList<String>                   propertyFiles                = new ArrayList<String>();

   @Builder.Default
   private ArrayList<String>                   testCatalogFragmentFiles     = new ArrayList<String>();

   private SSLProperties                       sslProperties;

   @Builder.Default
   private KartaMinionConfiguration            localNode                    = new KartaMinionConfiguration();

   @Builder.Default
   private ArrayList<KartaMinionConfiguration> nodes                        = new ArrayList<KartaMinionConfiguration>();

   @Builder.Default
   private int                                 testThreadCount              = 1;

   @Builder.Default
   private boolean                             minionsEnabled               = true;

   @Builder.Default
   private ArrayList<String>                   configurationScanPackages    = new ArrayList<String>();

   public synchronized void expandSystemAndEnvProperties()
   {
      PropertyUtils.expandEnvVars( pluginsDirectories );
      defaultFeatureSourceParserPlugin = PropertyUtils.expandEnvVars( defaultFeatureSourceParserPlugin );
      defaultStepRunnerPlugin = PropertyUtils.expandEnvVars( defaultStepRunnerPlugin );
      PropertyUtils.expandEnvVars( defaultTestDataSourcePlugins );
      PropertyUtils.expandEnvVars( enabledPlugins );
      PropertyUtils.expandEnvVars( propertyFiles );
      PropertyUtils.expandEnvVars( testCatalogFragmentFiles );
      sslProperties.expandSystemAndEnvProperties();;
      PropertyUtils.expandEnvVars( configurationScanPackages );
   }
}
