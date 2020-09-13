package org.mvss.karta.server.beans;

import java.io.IOException;
import java.net.URISyntaxException;

import org.mvss.karta.framework.runtime.RuntimeConfiguration;
import org.mvss.karta.framework.utils.ClassPathLoaderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ConfigBeans
{
   private static final String RUN_CONFIGURATION_FILE_NAME = "runConfiguration.json";

   @Autowired
   private ObjectMapper        objectMapper;

   @Bean
   public RuntimeConfiguration getRunConfiguration()
   {
      RuntimeConfiguration runtimeConfiguration = null;
      try
      {
         // TODO: Handle IO Exception
         runtimeConfiguration = objectMapper.readValue( ClassPathLoaderUtils.readAllText( RUN_CONFIGURATION_FILE_NAME ), RuntimeConfiguration.class );
      }
      catch ( IOException | URISyntaxException e )
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return runtimeConfiguration;
   }
}
