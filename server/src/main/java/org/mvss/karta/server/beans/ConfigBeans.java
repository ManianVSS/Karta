package org.mvss.karta.server.beans;

import org.mvss.karta.framework.runtime.Configurator;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.RuntimeConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigBeans
{
   @Bean
   public KartaRuntime getKartaRuntime()
   {
      return KartaRuntime.getInstance();
   }

   @Bean
   public RuntimeConfiguration getRunConfiguration()
   {
      return KartaRuntime.getInstance().getRuntimeConfiguration();
   }

   @Bean
   public Configurator getConfigurator()
   {
      return KartaRuntime.getInstance().getConfigurator();
   }
}
