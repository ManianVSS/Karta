package org.mvss.karta.framework.runtime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mvss.karta.configuration.PluginConfig;
import org.mvss.karta.framework.runtime.interfaces.Plugin;
import org.mvss.karta.framework.utils.DynamicClassLoader;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PnPRegistry
{
   private static TypeReference<ArrayList<PluginConfig>>             pluginConfigArrayListType = new TypeReference<ArrayList<PluginConfig>>()
                                                                                               {
                                                                                               };

   private HashMap<Class<? extends Plugin>, HashMap<String, Plugin>> pluginMap                 = new HashMap<Class<? extends Plugin>, HashMap<String, Plugin>>();
   private HashMap<String, Plugin>                                   registeredPlugins         = new HashMap<String, Plugin>();

   private ArrayList<Class<? extends Plugin>>                        pluginTypes               = new ArrayList<Class<? extends Plugin>>();

   public void addPluginType( Class<? extends Plugin> pluginType )
   {
      pluginTypes.add( pluginType );
   }

   public boolean registerPlugin( File jarFile, PluginConfig pluginConfig ) throws Throwable
   {
      log.debug( "Registering plugin " + pluginConfig );

      @SuppressWarnings( "unchecked" )
      Class<? extends Plugin> pluginClass = ( jarFile != null ) ? (Class<? extends Plugin>) DynamicClassLoader.loadClass( jarFile, pluginConfig.getClassName() ) : (Class<? extends Plugin>) Class.forName( pluginConfig.getClassName() );

      boolean isRegisteredPluginType = false;

      for ( Class<? extends Plugin> pluginType : pluginTypes )
      {
         if ( pluginType.isAssignableFrom( pluginClass ) )
         {
            isRegisteredPluginType = true;

            if ( !pluginMap.containsKey( pluginType ) )
            {
               pluginMap.put( pluginType, new HashMap<String, Plugin>() );
            }

            HashMap<String, Plugin> pluginsOfSpecifiedType = pluginMap.get( pluginType );

            if ( pluginsOfSpecifiedType.containsKey( pluginConfig.getPluginName() ) )
            {
               return false;
            }

            Plugin plugin = null;

            if ( registeredPlugins.containsKey( pluginConfig.getPluginName() ) )
            {
               plugin = registeredPlugins.get( pluginConfig.getPluginName() );
            }
            else
            {
               plugin = pluginClass.newInstance();
               registeredPlugins.put( pluginConfig.getPluginName(), plugin );
            }
            pluginsOfSpecifiedType.put( pluginConfig.getPluginName(), plugin );
         }
      }

      // Unregistered pluggin type
      return isRegisteredPluginType;
   }

   public void addPluginConfiguration( File jarFile, ArrayList<PluginConfig> pluginConfigs )
   {
      for ( PluginConfig pluginConfig : pluginConfigs )
      {
         try
         {
            if ( !registerPlugin( jarFile, pluginConfig ) )
            {
               log.error( "Plugin registration failed for " + pluginConfig );
            }
         }
         catch ( Throwable t )
         {
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
      String pluginConfigStr = IOUtils.toString( DynamicClassLoader.getClassPathResourceInJarAsStream( jarFile, Constants.KARTA_PLUGINS_CONFIG_JSON ), Charset.defaultCharset() );
      ArrayList<PluginConfig> pluginConfigs = ParserUtils.getObjectMapper().readValue( pluginConfigStr, pluginConfigArrayListType );
      addPluginConfiguration( jarFile, pluginConfigs );

      InputStream runtimePropertiesInputStream = DynamicClassLoader.getClassPathResourceInJarAsStream( jarFile, Constants.KARTA_RUNTIME_PROPERTIES_JSON );

      if ( ( configurator != null ) && ( runtimePropertiesInputStream != null ) )
      {
         String runtimePropertiesStr = IOUtils.toString( runtimePropertiesInputStream, Charset.defaultCharset() );
         HashMap<String, HashMap<String, Serializable>> runtimeProperties = Configurator.readPropertiesFromString( runtimePropertiesStr );
         configurator.mergeProperties( runtimeProperties );
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

   public void initializePlugins( HashMap<String, HashMap<String, Serializable>> runProperties )
   {
      for ( Plugin plugin : registeredPlugins.values() )
      {
         try
         {
            plugin.initialize( runProperties );
         }
         catch ( Throwable t )
         {
            log.error( "Plugin failed to initialize: " + plugin.getPluginName(), t );
         }
      }
   }

   public Plugin getPlugin( String name, Class<? extends Plugin> pluginType )
   {
      return pluginMap.containsKey( pluginType ) ? pluginMap.get( pluginType ).get( name ) : null;
   }

}
