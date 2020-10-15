package org.mvss.karta.framework.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.Constants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
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
   private static XmlMapper                                         xmlMapper                = new XmlMapper();

   @Getter
   private static BeanUtilsBean                                     nullAwareBeanUtils       = new NullAwareBeanUtilsBean();

   static
   {
      objectMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
      yamlObjectMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
      xmlMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
   }

   public static ArrayList<String> parseListOfStringFromJson( String source ) throws JsonMappingException, JsonProcessingException
   {
      return objectMapper.readValue( source, arrayListOfStringType );
   }

   public static ArrayList<String> parseListOfStringFromYaml( String source ) throws JsonMappingException, JsonProcessingException
   {
      return yamlObjectMapper.readValue( source, arrayListOfStringType );
   }

   public static <T> T readValue( DataFormat format, String content, TypeReference<T> valueTypeRef ) throws JsonMappingException, JsonProcessingException
   {
      switch ( format )
      {
         case JSON:
            return objectMapper.readValue( content, valueTypeRef );
         case XML:
            return xmlMapper.readValue( content, valueTypeRef );

         default:
         case YAML:
            return yamlObjectMapper.readValue( content, valueTypeRef );
      }
   }

   public static <T> T readValue( DataFormat format, String content, Class<T> valueType ) throws JsonProcessingException, JsonMappingException
   {
      switch ( format )
      {
         case JSON:
            return objectMapper.readValue( content, valueType );
         case XML:
            return xmlMapper.readValue( content, valueType );

         default:
         case YAML:
            return yamlObjectMapper.readValue( content, valueType );
      }
   }

   public static <T> T convertValue( DataFormat format, Object fromValue, TypeReference<T> valueTypeRef )
   {
      switch ( format )
      {
         case JSON:
            return objectMapper.convertValue( fromValue, valueTypeRef );
         case XML:
            return xmlMapper.convertValue( fromValue, valueTypeRef );

         default:
         case YAML:
            return yamlObjectMapper.convertValue( fromValue, valueTypeRef );
      }
   }

   public static <T> T convertValue( DataFormat format, Object fromValue, Class<T> toValueType )
   {
      switch ( format )
      {
         case JSON:
            return objectMapper.convertValue( fromValue, toValueType );
         case XML:
            return xmlMapper.convertValue( fromValue, toValueType );

         default:
         case YAML:
            return yamlObjectMapper.convertValue( fromValue, toValueType );
      }
   }

   public static DataFormat getFileDataFormat( String fileName )
   {
      String fileExtension = ( fileName.contains( Constants.DOT ) && !fileName.endsWith( Constants.DOT ) ) ? fileName.toLowerCase().substring( fileName.lastIndexOf( Constants.DOT ) + 1 ) : Constants.EMPTY_STRING;
      if ( fileExtension.equals( Constants.JSON ) )
      {
         return DataFormat.JSON;
      }
      else if ( fileExtension.equals( Constants.XML ) )
      {
         return DataFormat.XML;
      }
      else// if ( fileExtension.equals( Constants.YAML ) || fileExtension.equals( Constants.YML ) )
      {
         return DataFormat.YAML;
      }
   }
}
