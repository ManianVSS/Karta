package org.mvss.karta.framework.configuration;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.models.catalog.Test;
import org.mvss.karta.framework.models.catalog.TestCategory;
import org.mvss.karta.framework.nodes.IKartaNodeRegistry;
import org.mvss.karta.framework.nodes.KartaNodeConfiguration;
import org.mvss.karta.framework.nodes.KartaNodeRegistry;
import org.mvss.karta.framework.plugins.impl.*;
import org.mvss.karta.framework.plugins.impl.kriya.KriyaPlugin;
import org.mvss.karta.framework.properties.Configurator;
import org.mvss.karta.framework.utils.DataUtils;
import org.mvss.karta.framework.utils.NullAwareBeanUtilsBean;
import org.mvss.karta.framework.utils.PropertyUtils;
import org.mvss.karta.framework.utils.SSLProperties;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
public class KartaConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The list of plug-in details
     *
     * @see PluginConfig
     */
    @Builder.Default
    private ArrayList<PluginConfig> pluginConfigurations = new ArrayList<>();

    /**
     * The default feature source parser plug-in
     */
    private String defaultFeatureSourceParser;

    /**
     * The default step runner plug-in
     */
    @Builder.Default
    private HashSet<String> defaultStepRunners = new HashSet<>();//String defaultStepRunner;

    /**
     * The default set of test data source plug-ins
     */
    @Builder.Default
    private HashSet<String> defaultTestDataSources = new HashSet<>();

    /**
     * The set of plug-in which are enabled that are to be initialized and closed with Karta Runtime
     */
    private HashSet<String> enabledPlugins;

    /**
     * The list of property files to be merged into the Configurator.</br>
     * The latter ones in sequence override duplicate properties during merge </br>
     */
    @Builder.Default
    private ArrayList<String> propertyFiles = new ArrayList<>();

    /**
     * The list of test catalog fragment files to merge into the TestCatalog.</br>
     * For schema of file, refer to {@link TestCategory}
     */
    @Builder.Default
    private ArrayList<String> testCatalogFragmentFiles = new ArrayList<>();

    /**
     * The SSL configuration (Java trust store and keystore) for Karta. </br>
     *
     * @see org.mvss.karta.framework.utils.SSLProperties
     */
    private SSLProperties sslProperties;

    /**
     * The karta node registry implementation class
     */
    private String kartaNodeRegistry;

    //   /**
    //    * The current node configuration.</br>
    //    * This is mandatory if running a minion server or node.
    //    *
    //    * @see org.mvss.karta.framework.nodes.KartaNodeConfiguration
    //    */
    //   @Builder.Default
    //   private KartaNodeConfiguration localNode = new KartaNodeConfiguration();

    /**
     * The list of available nodes available or minions to use. </br>
     *
     * @see org.mvss.karta.framework.nodes.KartaNodeConfiguration
     */
    @Builder.Default
    private HashSet<KartaNodeConfiguration> nodes = new HashSet<>();

    /**
     * Indicates if minions are enabled to run scenario iterations for load sharing.
     */
    @Builder.Default
    private Boolean minionsEnabled = true;

    /**
     * The map of thread group name and respective thread count.</br>
     * An exclusive test thread group should always run in one thread.</br>
     * Refer {@link Test#getThreadGroup}
     */
    @Builder.Default
    private HashMap<String, Integer> threadGroups = new HashMap<>();

    /**
     * The list of Java package names to scan for {@link org.mvss.karta.framework.annotations.KartaBean} annotations on public and static methods. </br>
     */
    @Builder.Default
    private ArrayList<String> configurationScanPackages = new ArrayList<>();

    /**
     * Properties to load. This is a mapping of group name to the map of property names to Serializable property values.
     */
    @Builder.Default
    private HashMap<String, HashMap<String, Serializable>> properties = new HashMap<>();

    /**
     * Indicates if detailed reporting is to be disabled by trimming.
     */
    @Builder.Default
    private Boolean detailedReport = true;

    public static synchronized KartaConfiguration getDefaultConfiguration() {
        KartaConfiguration kartaConfiguration = new KartaConfiguration();

        kartaConfiguration.pluginConfigurations.add(new PluginConfig(Constants.KRIYA, KriyaPlugin.class.getName(), null));
        kartaConfiguration.pluginConfigurations.add(new PluginConfig(Constants.DATA_FILES_TEST_DATA_SOURCE, DataFilesTestDataSource.class.getName(), null));
        kartaConfiguration.pluginConfigurations.add(new PluginConfig(Constants.OBJECT_GEN_TEST_DATA_SOURCE, ObjectGenTestDataSource.class.getName(), null));
        kartaConfiguration.pluginConfigurations.add(new PluginConfig(Constants.LOGGING_TEST_EVENT_LISTENER, LoggingTestEventListener.class.getName(), null));
        kartaConfiguration.pluginConfigurations.add(new PluginConfig(Constants.DUMP_TO_FILE_TEST_EVENT_LISTENER, DumpToFileTestEventListener.class.getName(), null));
        kartaConfiguration.pluginConfigurations.add(new PluginConfig(Constants.RABBIT_MQ_TEST_EVENT_LISTENER, RabbitMQTestEventListener.class.getName(), null));
        kartaConfiguration.pluginConfigurations.add(new PluginConfig(Constants.HTML_REPORT_TEST_EVENT_LISTENER, HTMLReportTestEventListener.class.getName(), null));

        kartaConfiguration.enabledPlugins = new HashSet<>();
        kartaConfiguration.enabledPlugins.add(Constants.KRIYA);
        kartaConfiguration.enabledPlugins.add(Constants.DATA_FILES_TEST_DATA_SOURCE);
        kartaConfiguration.enabledPlugins.add(Constants.OBJECT_GEN_TEST_DATA_SOURCE);
        kartaConfiguration.enabledPlugins.add(Constants.LOGGING_TEST_EVENT_LISTENER);

        kartaConfiguration.defaultFeatureSourceParser = Constants.KRIYA;
        kartaConfiguration.defaultStepRunners.add(Constants.KRIYA);
        kartaConfiguration.defaultTestDataSources.add(Constants.DATA_FILES_TEST_DATA_SOURCE);

        kartaConfiguration.propertyFiles.add(Constants.KARTA_PROPERTIES_YAML);
        kartaConfiguration.propertyFiles.add(Constants.KARTA_PLUGIN_PROPERTIES_YAML);
        kartaConfiguration.propertyFiles.add(Constants.KARTA_TEST_PROPERTIES);

        kartaConfiguration.sslProperties = new SSLProperties();

        kartaConfiguration.kartaNodeRegistry = KartaNodeRegistry.class.getName();
        kartaConfiguration.minionsEnabled = true;

        kartaConfiguration.threadGroups.put(Constants.__DEFAULT__, 1);

        kartaConfiguration.detailedReport = true;
        return kartaConfiguration;
    }

    /**
     * Expands system and environmental variables into keys configuration value. </br>
     * Refer {@link org.mvss.karta.framework.utils.PropertyUtils#propertyPattern}.
     */
    public synchronized void expandSystemAndEnvProperties() {
        // TODO: Change to a generic utility for expanding env vars with annotations
        defaultFeatureSourceParser = PropertyUtils.expandEnvVars(defaultFeatureSourceParser);
        PropertyUtils.expandEnvVars(defaultStepRunners);
        PropertyUtils.expandEnvVars(defaultTestDataSources);
        PropertyUtils.expandEnvVars(enabledPlugins);
        PropertyUtils.expandEnvVars(propertyFiles);
        PropertyUtils.expandEnvVars(testCatalogFragmentFiles);
        sslProperties.expandSystemAndEnvProperties();
        PropertyUtils.expandEnvVars(configurationScanPackages);
    }

    public synchronized void overrideConfiguration(KartaConfiguration override) {
        // TODO: Change to a generic utility to copy properties with an annotation for mapping
        DataUtils.addMissing(pluginConfigurations, override.pluginConfigurations);
        defaultFeatureSourceParser = NullAwareBeanUtilsBean.getOverriddenValue(defaultFeatureSourceParser, override.defaultFeatureSourceParser);
        DataUtils.addMissing(defaultStepRunners, override.defaultStepRunners);
        DataUtils.addMissing(defaultTestDataSources, override.defaultTestDataSources);
        DataUtils.addMissing(enabledPlugins, override.enabledPlugins);
        DataUtils.addMissing(propertyFiles, override.propertyFiles);
        DataUtils.addMissing(testCatalogFragmentFiles, override.testCatalogFragmentFiles);
        sslProperties = NullAwareBeanUtilsBean.getOverriddenValue(sslProperties, override.sslProperties);
        kartaNodeRegistry = NullAwareBeanUtilsBean.getOverriddenValue(kartaNodeRegistry, override.kartaNodeRegistry);
        //      localNode     = NullAwareBeanUtilsBean.getOverriddenValue( localNode, override.localNode );
        DataUtils.addMissing(nodes, override.nodes);
        minionsEnabled = override.minionsEnabled;
        DataUtils.mergeMapInto(override.threadGroups, threadGroups);
        DataUtils.addMissing(configurationScanPackages, override.configurationScanPackages);
        Configurator.mergeProperties(properties, override.properties);
        detailedReport = override.detailedReport;
    }

    public IKartaNodeRegistry createNodeRegistry() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if (StringUtils.isEmpty(this.kartaNodeRegistry)) {
            this.kartaNodeRegistry = KartaNodeRegistry.class.getName();
        }

        Class<?> nodeRegistryClass = Class.forName(kartaNodeRegistry);
        return (IKartaNodeRegistry) nodeRegistryClass.getDeclaredConstructor().newInstance();
    }
}
