package org.mvss.karta.server.beans;

import java.io.IOException;
import java.net.URISyntaxException;

import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.RuntimeConfiguration;
import org.mvss.karta.framework.utils.ClassPathLoaderUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Configuration
public class ConfigBeans
{
   @Autowired
   private ObjectMapper objectMapper;

   @Bean
   public RuntimeConfiguration getRunConfiguration()
   {
      RuntimeConfiguration runtimeConfiguration = null;
      try
      {
         runtimeConfiguration = objectMapper.readValue( ClassPathLoaderUtils.readAllText( Constants.RUN_CONFIGURATION_FILE_NAME ), RuntimeConfiguration.class );
      }
      catch ( IOException | URISyntaxException e )
      {
         log.error( e );
      }
      return runtimeConfiguration;
   }
}
