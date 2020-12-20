package org.mvss.karta.server.beans;

import org.mvss.karta.framework.runtime.KartaRuntime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
public class ConfigBeans
{
   @Bean
   public KartaRuntime getKartaRuntime()
   {
      try
      {
         KartaRuntime.initializeNodes = false;
         KartaRuntime kartaRuntime = KartaRuntime.getInstance();

         if ( kartaRuntime == null )
         {
            log.error( "Karta runtime could not be initialized. Please check the directory and config files" );
            System.exit( -1 );
         }

         return kartaRuntime;
      }
      catch ( Throwable e )
      {
         log.error( e );
         System.exit( 1 );
      }
      return null;
   }

   // @Bean
   // public KartaConfiguration getKartaConfiguration()
   // {
   // try
   // {
   // return KartaRuntime.getInstance().getKartaConfiguration();
   // }
   // catch ( Throwable e )
   // {
   // log.error( e );
   // System.exit( 1 );
   // }
   // return null;
   // }
   //
   // @Bean
   // public Configurator getConfigurator()
   // {
   // try
   // {
   // return KartaRuntime.getInstance().getConfigurator();
   // }
   // catch ( Throwable e )
   // {
   // log.error( e );
   // System.exit( 1 );
   // }
   // return null;
   // }
}
