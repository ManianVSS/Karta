package org.mvss.karta.configuration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.mvss.karta.framework.nodes.KartaNodeConfiguration;
import org.mvss.karta.framework.runtime.Configurator;
import org.mvss.karta.framework.utils.DataUtils;
import org.mvss.karta.framework.utils.NullAwareBeanUtilsBean;
import org.mvss.karta.framework.utils.PropertyUtils;
import org.mvss.karta.framework.utils.SSLProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Configuration for Karta.</br>
 * 
 * @author Manian
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KartaConfiguration implements Serializable
{
   private static final long                              serialVersionUID           = 1L;

   /**
    * The list of plug-in details
    * 
    * @see PluginConfig
    */
   @Builder.Default
   private ArrayList<PluginConfig>                        pluginConfigurations       = new ArrayList<PluginConfig>();

   /**
    * The default feature source parser plug-in
    */
   @Builder.Default
   private String                                         defaultFeatureSourceParser = null;

   /**
    * The default step runner plug-in
    */
   @Builder.Default
   private String                                         defaultStepRunner          = null;

   /**
    * The default set of test data source plug-ins
    */
   @Builder.Default
   private HashSet<String>                                defaultTestDataSources     = new HashSet<String>();

   /**
    * The set of plug-in which are enabled that are to be initialized and closed with Karta Runtime
    */
   @Builder.Default
   private HashSet<String>                                enabledPlugins             = null;

   /**
    * The list of property files to be merged into the Configurator.</br>
    * The latter ones in sequence override duplicate properties during merge </br>
    */
   @Builder.Default
   private ArrayList<String>                              propertyFiles              = new ArrayList<String>();

   /**
    * The list of test catalog fragment files to merge into the TestCatalog.</br>
    * For schema of files refer to {@link org.mvss.karta.framework.runtime.testcatalog.TestCategory}
    */
   @Builder.Default
   private ArrayList<String>                              testCatalogFragmentFiles   = new ArrayList<String>();

   /**
    * The SSL configuration (Java trust store and keystore) for Karta. </br>
    * 
    * @see org.mvss.karta.framework.utils.SSLProperties
    */
   @Builder.Default
   private SSLProperties                                  sslProperties              = null;

   /**
    * The current node configuration.</br>
    * This is mandatory if running a minion server or node.
    * 
    * @see org.mvss.karta.framework.nodes.KartaNodeConfiguration
    */
   @Builder.Default
   private KartaNodeConfiguration                         localNode                  = new KartaNodeConfiguration();

   /**
    * The list of available nodes available or minions to use. </br>
    * 
    * @see org.mvss.karta.framework.nodes.KartaNodeConfiguration
    */
   @Builder.Default
   private HashSet<KartaNodeConfiguration>                nodes                      = new HashSet<KartaNodeConfiguration>();

   /**
    * Indicates if minions are enabled to run scenario iterations for load sharing.
    */
   @Builder.Default
   private boolean                                        minionsEnabled             = true;

   /**
    * The map of thread group name and respective thread count.</br>
    * An exclusive test thread group should always run in one thread.</br>
    * Refer {@link org.mvss.karta.framework.runtime.testcatalog.Test#threadGroup}
    */
   @Builder.Default
   private HashMap<String, Integer>                       threadGroups               = new HashMap<String, Integer>();

   /**
    * The list of Java package names to scan for {@link org.mvss.karta.framework.core.KartaBean} annotations on public and static methods. </br>
    */
   @Builder.Default
   private ArrayList<String>                              configurationScanPackages  = new ArrayList<String>();

   /**
    * Properties to load. This is a mapping of group name to the map of property names to Serializable property values.
    */
   @Builder.Default
   private HashMap<String, HashMap<String, Serializable>> properties                 = new HashMap<String, HashMap<String, Serializable>>();

   /**
    * Expands system and environmental variables into keys configuration value. </br>
    * Refer {@link org.mvss.karta.framework.utils.PropertyUtils#propertyPattern}.
    */
   public synchronized void expandSystemAndEnvProperties()
   {
      // TODO: Change to a generic utility for expanding env vars with annotations
      defaultFeatureSourceParser = PropertyUtils.expandEnvVars( defaultFeatureSourceParser );
      defaultStepRunner = PropertyUtils.expandEnvVars( defaultStepRunner );
      PropertyUtils.expandEnvVars( defaultTestDataSources );
      PropertyUtils.expandEnvVars( enabledPlugins );
      PropertyUtils.expandEnvVars( propertyFiles );
      PropertyUtils.expandEnvVars( testCatalogFragmentFiles );
      sslProperties.expandSystemAndEnvProperties();;
      PropertyUtils.expandEnvVars( configurationScanPackages );
   }

   public synchronized void overrideConfiguration( KartaConfiguration override )
   {
      // TODO: Change to a generic utility to copy properties with an annotation for mapping
      DataUtils.addMissing( pluginConfigurations, override.pluginConfigurations );
      defaultFeatureSourceParser = NullAwareBeanUtilsBean.getOverridenValue( defaultFeatureSourceParser, override.defaultFeatureSourceParser );
      defaultStepRunner = NullAwareBeanUtilsBean.getOverridenValue( defaultStepRunner, override.defaultStepRunner );
      DataUtils.addMissing( defaultTestDataSources, override.defaultTestDataSources );
      DataUtils.addMissing( enabledPlugins, override.enabledPlugins );
      DataUtils.addMissing( propertyFiles, override.propertyFiles );
      DataUtils.addMissing( testCatalogFragmentFiles, override.testCatalogFragmentFiles );
      sslProperties = NullAwareBeanUtilsBean.getOverridenValue( sslProperties, override.sslProperties );
      localNode = NullAwareBeanUtilsBean.getOverridenValue( localNode, override.localNode );
      DataUtils.addMissing( nodes, override.nodes );
      minionsEnabled = override.minionsEnabled;
      DataUtils.mergeMapInto( override.threadGroups, threadGroups );
      DataUtils.addMissing( configurationScanPackages, override.configurationScanPackages );
      Configurator.mergeProperties( properties, override.properties );
   }
}
