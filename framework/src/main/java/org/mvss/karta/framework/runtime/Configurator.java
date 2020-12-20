package org.mvss.karta.framework.runtime;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.utils.AnnotationScanner;
import org.mvss.karta.framework.utils.ClassPathLoaderUtils;
import org.mvss.karta.framework.utils.ParserUtils;
import org.mvss.karta.framework.utils.PropertyUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.Getter;

/**
 * This class is used to load properties (Serializable fields) of an object or class.
 * The properties serialization format supported are YAML, JSON and XML.
 * The properties are merged into a properties store which is a map of property group
 * to map of property name to property values.
 * 
 * @author Manian
 */
public class Configurator
{
   public static final TypeReference<HashMap<String, HashMap<String, Serializable>>> propertiesType  = new TypeReference<HashMap<String, HashMap<String, Serializable>>>()
                                                                                                     {
                                                                                                     };
   /**
    * Property Story is a mapping of group name to the map of property names to Serializable property values.
    */
   @Getter
   private HashMap<String, HashMap<String, Serializable>>                            propertiesStore = new HashMap<String, HashMap<String, Serializable>>();

   /**
    * Merges a property store into the configurator's property store.
    * 
    * @param propertiesToMerge
    */
   public void mergeProperties( HashMap<String, HashMap<String, Serializable>> propertiesToMerge )
   {
      for ( String propertyGroupToMerge : propertiesToMerge.keySet() )
      {
         if ( !propertiesStore.containsKey( propertyGroupToMerge ) )
         {
            propertiesStore.put( propertyGroupToMerge, new HashMap<String, Serializable>() );
         }

         HashMap<String, Serializable> propertiesStoreGroup = propertiesStore.get( propertyGroupToMerge );
         HashMap<String, Serializable> propertiesToMergeForGroup = propertiesToMerge.get( propertyGroupToMerge );

         for ( String propertyToMerge : propertiesToMergeForGroup.keySet() )
         {
            propertiesStoreGroup.put( propertyToMerge, propertiesToMergeForGroup.get( propertyToMerge ) );
         }
      }
   }

   /**
    * Read property store from String based on the data format
    * 
    * @param dataFormat
    * @param propertiesDataString
    * @return
    * @throws JsonMappingException
    * @throws JsonProcessingException
    */
   public static HashMap<String, HashMap<String, Serializable>> readPropertiesFromString( DataFormat dataFormat, String propertiesDataString ) throws JsonMappingException, JsonProcessingException
   {
      return ParserUtils.readValue( dataFormat, propertiesDataString, propertiesType );
   }

   /**
    * Merge property store parsed from the string based on the data format
    * 
    * @param dataFormat
    * @param propertiesDataString
    * @throws IOException
    * @throws URISyntaxException
    */
   public void mergePropertiesString( DataFormat dataFormat, String propertiesDataString ) throws IOException, URISyntaxException
   {
      HashMap<String, HashMap<String, Serializable>> propertiesToMerge = readPropertiesFromString( dataFormat, propertiesDataString );
      mergeProperties( propertiesToMerge );
   }

   /**
    * Merge multiple property files to the data store inferencing data format from the file extension.
    * 
    * @param propertyFiles
    * @throws IOException
    * @throws URISyntaxException
    */
   public void mergePropertiesFiles( String... propertyFiles ) throws IOException, URISyntaxException
   {
      for ( String propertyFile : propertyFiles )
      {
         mergePropertiesString( ParserUtils.getFileDataFormat( propertyFile ), ClassPathLoaderUtils.readAllText( propertyFile ) );
      }
   }

   /**
    * Cached map of environment properties.
    */
   public static Map<String, String> envPropMap    = System.getenv();
   /**
    * Cached map of system properties.
    */
   public static Properties          systemPropMap = System.getProperties();

   /**
    * Fetch property value by group name and property name.
    * 
    * @param group
    * @param name
    * @return
    */
   public Serializable getPropertyValue( String group, String name )
   {
      return PropertyUtils.getPropertyValue( propertiesStore, group, name );
   }

   /**
    * Load properties into the object.
    * 
    * @param propertiesStore
    * @param object
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    */
   public static void loadProperties( HashMap<String, HashMap<String, Serializable>> propertiesStore, Object object ) throws IllegalArgumentException, IllegalAccessException
   {
      AnnotationScanner.forEachField( object.getClass(), PropertyMapping.class, AnnotationScanner.IS_NON_STATIC
               .and( AnnotationScanner.IS_NON_FINAL ), null, ( type, field, annotation ) -> PropertyUtils.setFieldValue( propertiesStore, object, field, (PropertyMapping) annotation ) );
   }

   /**
    * Load properties into multiple objects.
    * 
    * @param objects
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    */
   public void loadProperties( Object... objects ) throws IllegalArgumentException, IllegalAccessException
   {
      for ( Object object : objects )
      {
         loadProperties( propertiesStore, object );
      }
   }

   /**
    * Load properties to static fields of the class
    * 
    * @param propertiesStore
    * @param classToLoadPropertiesWith
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    */
   public void loadProperties( HashMap<String, HashMap<String, Serializable>> propertiesStore, Class<?> classToLoadPropertiesWith ) throws IllegalArgumentException, IllegalAccessException
   {
      AnnotationScanner.forEachField( classToLoadPropertiesWith, PropertyMapping.class, AnnotationScanner.IS_STATIC
               .and( AnnotationScanner.IS_NON_FINAL ), null, ( type, field, annotation ) -> PropertyUtils.setFieldValue( propertiesStore, null, field, (PropertyMapping) annotation ) );
   }

   /**
    * Load properties to static fields of the multiple class
    * 
    * @param classesToLoadPropertiesWith
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    */
   public void loadProperties( Class<?>... classesToLoadPropertiesWith ) throws IllegalArgumentException, IllegalAccessException
   {
      for ( Class<?> classToLoadPropertiesWith : classesToLoadPropertiesWith )
      {
         loadProperties( propertiesStore, classToLoadPropertiesWith );
      }
   }
}
