package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.configuration.KartaConfiguration;
import org.mvss.karta.configuration.PluginConfig;
import org.mvss.karta.framework.runtime.interfaces.FeatureSourceParser;
import org.mvss.karta.framework.runtime.interfaces.Plugin;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.utils.ExtensionLoader;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PnPRegistry
{
   private static final ExtensionLoader<Plugin>                             pluginLoader      = new ExtensionLoader<Plugin>();;

   private static HashMap<Class<? extends Plugin>, HashMap<String, Plugin>> pluginMap         = new HashMap<Class<? extends Plugin>, HashMap<String, Plugin>>();
   private static HashMap<String, Plugin>                                   registeredPlugins = new HashMap<String, Plugin>();

   private static ArrayList<Class<? extends Plugin>>                        pluginTypes       = new ArrayList<Class<? extends Plugin>>();

   static
   {
      pluginTypes.add( FeatureSourceParser.class );
      pluginTypes.add( StepRunner.class );
      pluginTypes.add( TestDataSource.class );
   }

   public static boolean registerPlugin( PluginConfig pluginConfig ) throws Throwable
   {
      log.debug( "Registering plugin " + pluginConfig );

      @SuppressWarnings( "unchecked" )
      Class<? extends Plugin> pluginClass = StringUtils.isNotBlank( pluginConfig.getJarFile() ) ? pluginLoader.LoadClass( pluginConfig.getJarFile(), pluginConfig.getClassName() ) : (Class<? extends Plugin>) Class.forName( pluginConfig.getClassName() );

      boolean isRegisteredPluginType = false;

      for ( Class<? extends Plugin> pluginType : pluginTypes )
      {
         if ( pluginType.isAssignableFrom( pluginClass ) )
         {
            isRegisteredPluginType = true;

            if ( !pluginMap.containsKey( pluginClass ) )
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

   public static void addConfiguration( KartaConfiguration kartaConfiguration )
   {
      ArrayList<PluginConfig> pluginConfigs = kartaConfiguration.getPluginConfigs();

      for ( PluginConfig pluginConfig : pluginConfigs )
      {

         try
         {
            if ( !registerPlugin( pluginConfig ) )
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

   public static void initializePlugins( HashMap<String, HashMap<String, Serializable>> pluginProperties ) throws Throwable
   {
      for ( Plugin plugin : registeredPlugins.values() )
      {
         if ( pluginProperties.containsKey( plugin.getPluginName() ) )
         {
            plugin.initialize( pluginProperties.get( plugin.getPluginName() ) );
         }
      }
   }

   public static Plugin getPlugin( String name, Class<? extends Plugin> pluginType )
   {
      return pluginMap.containsKey( pluginType ) ? pluginMap.get( pluginType ).get( name ) : null;
   }

}
