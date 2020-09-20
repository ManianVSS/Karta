package org.mvss.karta.server;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PreDestroy;

import org.mvss.karta.configuration.KartaConfiguration;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.PnPRegistry;
import org.mvss.karta.framework.runtime.RuntimeConfiguration;
import org.mvss.karta.framework.utils.ClassPathLoaderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootApplication
public class KartaApplication implements CommandLineRunner
{
   @Autowired
   private ObjectMapper         objectMapper;

   @Autowired
   private RuntimeConfiguration runtimeConfiguration;

   public static void main( String[] args )
   {
      // Spring boot start
      SpringApplication.run( KartaApplication.class, args );
   }

   public static List<Runnable> exitHooks = Collections.synchronizedList( new ArrayList<Runnable>() );

   @Override
   public void run( String... args ) throws Exception
   {
      log.info( "******************** Starting Karta Server *********************" );

      // TODO: Handle IO Exception
      KartaConfiguration kartaConfiguration = objectMapper.readValue( ClassPathLoaderUtils.readAllText( Constants.KARTA_CONFIG_FILE ), KartaConfiguration.class );
      PnPRegistry.addPluginConfiguration( kartaConfiguration.getPluginConfigs() );
      PnPRegistry.loadPlugins( new File( Constants.PLUGINS_DIRECTORY ) );
      try
      {
         PnPRegistry.initializePlugins( runtimeConfiguration.getPluginConfiguration() );
      }
      catch ( Throwable e )
      {
         log.error( e );
      }
   }

   @PreDestroy
   public void onDestroy() throws Exception
   {
      log.info( "******************** Stopping Karta Server *********************" );

      log.info( "Triggering registered exit hooks" );
      for ( Runnable exitHook : new ArrayList<Runnable>( exitHooks ) )
      {
         new Thread( exitHook ).run();
      }
   }

}