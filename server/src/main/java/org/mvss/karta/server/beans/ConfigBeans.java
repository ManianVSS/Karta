package org.mvss.karta.server.beans;

import org.mvss.karta.framework.runtime.Configurator;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.RuntimeConfiguration;
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
         return KartaRuntime.getInstance();
      }
      catch ( Throwable e )
      {
         log.error( e );
         System.exit( 1 );
      }
      return null;
   }

   @Bean
   public RuntimeConfiguration getRunConfiguration()
   {
      try
      {
         return KartaRuntime.getInstance().getRuntimeConfiguration();
      }
      catch ( Throwable e )
      {
         log.error( e );
         System.exit( 1 );
      }
      return null;
   }

   @Bean
   public Configurator getConfigurator()
   {
      try
      {
         return KartaRuntime.getInstance().getConfigurator();
      }
      catch ( Throwable e )
      {
         log.error( e );
         System.exit( 1 );
      }
      return null;
   }
}
