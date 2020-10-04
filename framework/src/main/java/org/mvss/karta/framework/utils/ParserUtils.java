package org.mvss.karta.framework.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.beanutils.BeanUtilsBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import lombok.Getter;

public class ParserUtils
{
   public static final TypeReference<HashMap<String, Serializable>> genericHashMapObjectType = new TypeReference<HashMap<String, Serializable>>()
                                                                                             {
                                                                                             };

   private static TypeReference<ArrayList<String>>                  arrayListOfStringType    = new TypeReference<ArrayList<String>>()
                                                                                             {
                                                                                             };

   @Getter
   private static ObjectMapper                                      objectMapper             = new ObjectMapper();

   @Getter
   private static ObjectMapper                                      yamlObjectMapper         = new ObjectMapper( new YAMLFactory() );;

   @Getter
   private static BeanUtilsBean                                     nullAwareBeanUtils       = new NullAwareBeanUtilsBean();

   static
   {
      objectMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
      yamlObjectMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
   }

   public static ArrayList<String> parseListOfStringFromJson( String source ) throws JsonMappingException, JsonProcessingException
   {
      return objectMapper.readValue( source, arrayListOfStringType );
   }

   public static ArrayList<String> parseListOfStringFromYaml( String source ) throws JsonMappingException, JsonProcessingException
   {
      return yamlObjectMapper.readValue( source, arrayListOfStringType );
   }
}
