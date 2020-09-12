package org.mvss.karta.runner.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ParserBeans
{
   @Bean
   public ObjectMapper getObjectMapper()
   {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
      return objectMapper;
   }
}
