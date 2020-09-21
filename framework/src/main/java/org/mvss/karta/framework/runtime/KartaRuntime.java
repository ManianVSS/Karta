package org.mvss.karta.framework.runtime;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;

import org.mvss.karta.configuration.KartaConfiguration;
import org.mvss.karta.framework.runtime.interfaces.FeatureSourceParser;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.testcatalog.TestCatalogManager;
import org.mvss.karta.framework.utils.ClassPathLoaderUtils;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class KartaRuntime
{
   @Getter
   private KartaConfiguration   kartaConfiguration;

   @Getter
   private RuntimeConfiguration runtimeConfiguration;

   @Getter
   private PnPRegistry          pnPRegistry;

   @Getter
   private Configurator         configurator;

   @Getter
   private TestCatalogManager   testCatalogManager;

   public void initializeRuntime() throws JsonMappingException, JsonProcessingException, IOException, URISyntaxException
   {
      ObjectMapper objectMapper = ParserUtils.getObjectMapper();

      // if ( kartaConfiguration == null )
      // {
      kartaConfiguration = objectMapper.readValue( ClassPathLoaderUtils.readAllText( Constants.KARTA_CONFIG_FILE ), KartaConfiguration.class );
      // }

      // if ( pnPRegistry == null )
      // {
      pnPRegistry = new PnPRegistry();

      // TODO: Move plugin types to Karta Configuration
      pnPRegistry.addPluginType( FeatureSourceParser.class );
      pnPRegistry.addPluginType( StepRunner.class );
      pnPRegistry.addPluginType( TestDataSource.class );
      // }

      pnPRegistry.addPluginConfiguration( kartaConfiguration.getPluginConfigs() );

      String pluginsDirectory = kartaConfiguration.getPluginsDirectory();

      if ( pluginsDirectory != null )
      {
         pnPRegistry.loadPlugins( new File( pluginsDirectory ) );
      }

      // if ( runtimeConfiguration == null )
      // {
      runtimeConfiguration = objectMapper.readValue( ClassPathLoaderUtils.readAllText( Constants.RUN_CONFIGURATION_FILE_NAME ), RuntimeConfiguration.class );
      // }

      // Configurator should be setup before plugin initialization

      // if ( configurator == null )
      // {
      configurator = new Configurator();
      // }

      HashSet<String> propertiesFileList = runtimeConfiguration.getPropertyFiles();
      if ( ( propertiesFileList != null ) && !propertiesFileList.isEmpty() )
      {
         String[] propertyFilesToLoad = new String[propertiesFileList.size()];
         propertiesFileList.toArray( propertyFilesToLoad );
         configurator.mergePropertiesFiles( propertyFilesToLoad );
      }

      pnPRegistry.initializePlugins( runtimeConfiguration.getPluginConfiguration() );

      testCatalogManager = new TestCatalogManager();
   }

   // Default constructor is reserved for default runtime instance use getDefaultInstance
   private KartaRuntime() throws JsonMappingException, JsonProcessingException, IOException, URISyntaxException
   {

   }

   private static KartaRuntime instance        = null;

   private static Object       _syncLockObject = new Object();

   public static KartaRuntime getInstance()
   {
      if ( instance == null )
      {
         synchronized ( _syncLockObject )
         {
            try
            {
               instance = new KartaRuntime();
               instance.initializeRuntime();
            }
            catch ( Throwable t )
            {
               return null;
            }
         }
      }

      return instance;
   }

   // TODO: Move running based on run target here
}
