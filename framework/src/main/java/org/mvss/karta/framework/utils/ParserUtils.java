package org.mvss.karta.framework.utils;

import org.apache.commons.beanutils.BeanUtilsBean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import lombok.Getter;

public class ParserUtils
{
   @Getter
   private static ObjectMapper  objectMapper       = new ObjectMapper();

   @Getter
   private static ObjectMapper  yamlObjectMapper   = new ObjectMapper( new YAMLFactory() );;

   @Getter
   private static BeanUtilsBean nullAwareBeanUtils = new NullAwareBeanUtilsBean();

   static
   {
      objectMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
      yamlObjectMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
   }
}
