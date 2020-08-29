package org.mvss.karta.framework.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ParserBeans
{
   @Bean
   public ObjectMapper getObjectMapper()
   {
      return new ObjectMapper();
   }
}
