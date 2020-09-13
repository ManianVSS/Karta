package org.mvss.karta.server.beans;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.mvss.karta.framework.utils.NullAwareBeanUtilsBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class UtilBeans
{
   @Bean
   public ObjectMapper getObjectMapper()
   {
      ObjectMapper objectMapper = new ObjectMapper();
      objectMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
      return objectMapper;
   }

   @Bean
   public BeanUtilsBean getBeanUtilsBean()
   {
      BeanUtilsBean notNull = new NullAwareBeanUtilsBean();
      return notNull;
   }
}
