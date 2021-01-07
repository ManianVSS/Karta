package org.mvss.karta.framework.runtime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mvss.karta.configuration.PluginConfig;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.interfaces.Plugin;
import org.mvss.karta.framework.utils.ClassPathLoaderUtils;
import org.mvss.karta.framework.utils.DynamicClassLoader;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PnPRegistry implements AutoCloseable
{
   @Getter
   private static TypeReference<ArrayList<PluginConfig>> pluginConfigArrayListType = new TypeReference<ArrayList<PluginConfig>>()
                                                                                   {
                                                                                   };

   private HashMap<String, Plugin>                       registeredPlugins         = new HashMap<String, Plugin>();
   private HashMap<String, Plugin>                       enabledPlugins            = new HashMap<String, Plugin>();

   public static ArrayList<PluginConfig> readPluginsConfig( String fileName ) throws JsonMappingException, JsonProcessingException, IOException, URISyntaxException
   {
      return ParserUtils.getYamlObjectMapper().readValue( ClassPathLoaderUtils.readAllText( fileName ), pluginConfigArrayListType );
   }

   public boolean registerPlugin( Plugin plugin )
   {
      if ( plugin == null )
      {
         return false;
      }

      String pluginName = plugin.getPluginName();

      log.info( "Registering plugin " + pluginName );

      if ( registeredPlugins.containsKey( pluginName ) )
      {
         log.warn( "Plugin already registered: " + pluginName );
         return false;
      }

      registeredPlugins.put( pluginName, plugin );

      return true;
   }

   public boolean registerPlugin( File jarFile, PluginConfig pluginConfig ) throws Throwable
   {
      if ( pluginConfig == null )
      {
         return false;
      }

      String pluginName = pluginConfig.getPluginName();

      if ( pluginName == null )
      {
         return false;
      }

      if ( registeredPlugins.containsKey( pluginName ) )
      {
         return false;
      }

      log.debug( "Registering plugin from configuration " + pluginConfig );
      @SuppressWarnings( "unchecked" )
      Class<? extends Plugin> pluginClass = ( jarFile != null ) ? (Class<? extends Plugin>) DynamicClassLoader.loadClass( jarFile, pluginConfig.getClassName() ) : (Class<? extends Plugin>) Class.forName( pluginConfig.getClassName() );
      Plugin plugin = pluginClass.newInstance();
      return registerPlugin( plugin );
   }

   public void addPluginConfiguration( File jarFile, ArrayList<PluginConfig> pluginConfigs )
   {
      for ( PluginConfig pluginConfig : pluginConfigs )
      {
         try
         {
            File evaluatedJarFile = ( jarFile == null ) ? ( ( pluginConfig.getJarFile() == null ) ? null : new File( pluginConfig.getJarFile() ) ) : jarFile;

            if ( !registerPlugin( evaluatedJarFile, pluginConfig ) )
            {
               log.error( "Plugin registration failed for " + pluginConfig );
            }
         }
         catch ( Throwable t )
         {
            log.error( Constants.EMPTY_STRING, t );
            continue;
         }
      }
   }

   public void addPluginConfiguration( ArrayList<PluginConfig> pluginConfigs )
   {
      addPluginConfiguration( null, pluginConfigs );
   }

   public void loadPluginJar( Configurator configurator, File jarFile ) throws MalformedURLException, IOException, URISyntaxException
   {
      InputStream jarFileInputStream = ( jarFile == null ) ? ClassPathLoaderUtils.getFileStream( Constants.KARTA_PLUGINS_CONFIG_YAML ) : DynamicClassLoader.getClassPathResourceInJarAsStream( jarFile, Constants.KARTA_PLUGINS_CONFIG_YAML );

      if ( jarFileInputStream == null )
      {
         return;
      }

      String pluginConfigStr = IOUtils.toString( jarFileInputStream, Charset.defaultCharset() );
      ArrayList<PluginConfig> pluginConfigs = ParserUtils.getYamlObjectMapper().readValue( pluginConfigStr, pluginConfigArrayListType );
      addPluginConfiguration( jarFile, pluginConfigs );

      if ( configurator != null )
      {
         InputStream runtimePropertiesInputStream = ( jarFile == null ) ? ClassPathLoaderUtils.getFileStream( Constants.KARTA_RUNTIME_PROPERTIES_YAML )
                  : DynamicClassLoader.getClassPathResourceInJarAsStream( jarFile, Constants.KARTA_RUNTIME_PROPERTIES_YAML );

         if ( runtimePropertiesInputStream != null )
         {
            String runtimePropertiesStr = IOUtils.toString( runtimePropertiesInputStream, Charset.defaultCharset() );
            HashMap<String, HashMap<String, Serializable>> runtimeProperties = Configurator.readPropertiesFromString( DataFormat.YAML, runtimePropertiesStr );
            configurator.mergeProperties( runtimeProperties );
         }
      }
   }

   public void loadPlugins( Configurator configurator, File pluginsDirectory )
   {
      for ( File jarFile : FileUtils.listFiles( pluginsDirectory, Constants.jarExtention, true ) )
      {
         try
         {
            loadPluginJar( configurator, jarFile );
         }
         catch ( Throwable t )
         {
            log.error( "Plugin failed to load: " + jarFile.getAbsolutePath(), t );
         }
      }
   }

   public void enablePlugin( String pluginName )
   {
      Plugin pluginToEnable = registeredPlugins.get( pluginName );

      if ( pluginToEnable != null )
      {
         enabledPlugins.put( pluginName, pluginToEnable );
      }
   }

   public void enablePlugins( HashSet<String> pluginNamesToEnable )
   {
      for ( String pluginName : pluginNamesToEnable )
      {
         enablePlugin( pluginName );
      }
   }

   public boolean initializePlugins( BeanRegistry beanRegistry, Configurator configurator )
   {
      for ( Entry<String, Plugin> pluginEntry : enabledPlugins.entrySet() )
      {
         Plugin plugin = pluginEntry.getValue();
         try
         {
            if ( beanRegistry != null )
            {
               beanRegistry.loadBeans( plugin );
            }

            if ( configurator != null )
            {
               configurator.loadProperties( plugin );
            }
            plugin.initialize();
         }
         catch ( Throwable t )
         {
            log.error( "Plugin failed to initialize: " + pluginEntry.getKey(), t );
            return false;
         }
      }
      return true;
   }

   @Override
   public void close()
   {
      for ( Entry<String, Plugin> pluginEntry : enabledPlugins.entrySet() )
      {
         Plugin plugin = pluginEntry.getValue();
         try
         {
            plugin.close();
         }
         catch ( Throwable t )
         {
            log.error( "Plugin failed to close: " + pluginEntry.getKey(), t );
         }
      }
   }

   public Plugin getPlugin( String name )
   {
      return registeredPlugins.get( name );
   }

   public Collection<Plugin> getEnabledPluginsOfType( Class<? extends Plugin> pluginType )
   {
      return getPluginsOfType( pluginType ).stream().filter( ( plugin ) -> enabledPlugins.values().contains( plugin ) ).collect( Collectors.toList() );
   }

   public Collection<Plugin> getPluginsOfType( Class<? extends Plugin> pluginType )
   {
      ArrayList<Plugin> plugins = new ArrayList<Plugin>();

      for ( Plugin plugin : registeredPlugins.values() )
      {
         if ( pluginType.isAssignableFrom( plugin.getClass() ) )
         {
            plugins.add( plugin );
         }
      }
      return plugins;
   }
}
