package org.mvss.karta.framework.utils;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.io.FilenameUtils;
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

/**
 * Utility class for parsing data and serializing to string for different formats (YAML, JSON, XML)
 * 
 * @author Manian
 */
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
   private static ObjectMapper                                      yamlObjectMapper         = new ObjectMapper( new YAMLFactory() );

   @Getter
   private static XmlMapper                                         xmlMapper                = new XmlMapper();

   @Getter
   private static BeanUtilsBean                                     nullAwareBeanUtils       = new NullAwareBeanUtilsBean();

   static
   {
      objectMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
      objectMapper.findAndRegisterModules();
      yamlObjectMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
      yamlObjectMapper.findAndRegisterModules();
      xmlMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
      xmlMapper.findAndRegisterModules();
   }

   /**
    * Parses a list of string from String (JSON source)
    * 
    * @param source
    * @return
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
   public static ArrayList<String> parseListOfStringFromJson( String source ) throws JsonMappingException, JsonProcessingException
   {
      return objectMapper.readValue( source, arrayListOfStringType );
   }

   /**
    * Parses a list of string from String (YAML source)
    * 
    * @param source
    * @return
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
   public static ArrayList<String> parseListOfStringFromYaml( String source ) throws JsonMappingException, JsonProcessingException
   {
      return yamlObjectMapper.readValue( source, arrayListOfStringType );
   }

   /**
    * Generic method to parse a serializable object of type T based on data format and type reference from the string source.
    * 
    * @param <T>
    * @param format
    * @param content
    * @param valueTypeRef
    * @return
    * @throws IOException
    */
   public static <T> T readValue( DataFormat format, String content, TypeReference<T> valueTypeRef ) throws IOException
   {
      switch ( format )
      {
         case JSON:
            return objectMapper.readValue( content, valueTypeRef );
         case XML:
            return xmlMapper.readValue( content, valueTypeRef );

         case PROPERTIES:
            try (StringReader stringReader = new StringReader( content ))
            {
               Properties properties = new Properties();
               properties.load( stringReader );
               return objectMapper.readValue( objectMapper.writeValueAsString( properties ), valueTypeRef );
            }

         default:
         case YAML:
            return yamlObjectMapper.readValue( content, valueTypeRef );
      }
   }

   /**
    * Generic method to parse a serializable object of type T based on data format and class from the string source.
    * 
    * @param <T>
    * @param format
    * @param content
    * @param valueType
    * @return
    * @throws IOException
    */
   public static <T> T readValue( DataFormat format, String content, Class<T> valueType ) throws IOException
   {
      switch ( format )
      {
         case JSON:
            return objectMapper.readValue( content, valueType );
         case XML:
            return xmlMapper.readValue( content, valueType );

         case PROPERTIES:
            try (StringReader stringReader = new StringReader( content ))
            {
               Properties properties = new Properties();
               properties.load( stringReader );
               return objectMapper.readValue( objectMapper.writeValueAsString( properties ), valueType );
            }

         default:
         case YAML:
            return yamlObjectMapper.readValue( content, valueType );
      }
   }

   /**
    * Generic method to convert an object to type T based on data format from another value of type reference .
    * 
    * @param <T>
    * @param format
    * @param fromValue
    * @param valueTypeRef
    * @return
    */
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

   /**
    * Generic method to convert an object to type T based on data format from another value of different class compatible with respect to object properties.
    * 
    * @param <T>
    * @param format
    * @param fromValue
    * @param toValueType
    * @return
    */
   @SuppressWarnings( "unchecked" )
   public static <T> T convertValue( DataFormat format, Object fromValue, Class<T> toValueType )
   {
      if ( fromValue.getClass() == toValueType )
      {
         return (T) fromValue;
      }

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

   /**
    * Get data format for file by name based on the file extension
    * 
    * @param fileName
    * @return
    */
   public static DataFormat getFileDataFormat( String fileName )
   {
      String fileExtension = FilenameUtils.getExtension( fileName );// ( fileName.contains( Constants.DOT ) && !fileName.endsWith( Constants.DOT ) ) ? fileName.toLowerCase().substring( fileName.lastIndexOf( Constants.DOT ) + 1 ) : Constants.EMPTY_STRING;
      if ( fileExtension.equals( Constants.JSON ) )
      {
         return DataFormat.JSON;
      }
      else if ( fileExtension.equals( Constants.XML ) )
      {
         return DataFormat.XML;
      }
      else if ( fileExtension.contentEquals( Constants.PROPERTIES ) )
      {
         return DataFormat.PROPERTIES;
      }
      else// if ( fileExtension.equals( Constants.YAML ) || fileExtension.equals( Constants.YML ) )
      {
         return DataFormat.YAML;
      }
   }

   public static String serializableToString( Serializable serializable )
   {
      if ( serializable.getClass() == String.class )
      {
         return (String) serializable;
      }

      try
      {
         return objectMapper.writeValueAsString( serializable );
      }
      catch ( JsonProcessingException jpe )
      {
         return null;
      }
   }

}
