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

public class Configurator
{
   public static final TypeReference<HashMap<String, HashMap<String, Serializable>>> propertiesType  = new TypeReference<HashMap<String, HashMap<String, Serializable>>>()
                                                                                                     {
                                                                                                     };

   @Getter
   private HashMap<String, HashMap<String, Serializable>>                            propertiesStore = new HashMap<String, HashMap<String, Serializable>>();

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

   public static HashMap<String, HashMap<String, Serializable>> readPropertiesFromString( DataFormat dataFormat, String propertiesDataString ) throws JsonMappingException, JsonProcessingException
   {
      return ParserUtils.readValue( dataFormat, propertiesDataString, propertiesType );
   }

   public void mergePropertiesString( DataFormat dataFormat, String propertiesDataString ) throws IOException, URISyntaxException
   {
      HashMap<String, HashMap<String, Serializable>> propertiesToMerge = readPropertiesFromString( dataFormat, propertiesDataString );
      mergeProperties( propertiesToMerge );
   }

   public void mergePropertiesFiles( String... propertyFiles ) throws IOException, URISyntaxException
   {
      for ( String propertyFile : propertyFiles )
      {
         mergePropertiesString( ParserUtils.getFileDataFormat( propertyFile ), ClassPathLoaderUtils.readAllText( propertyFile ) );
      }
   }

   public static Map<String, String> envPropMap    = System.getenv();
   public static Properties          systemPropMap = System.getProperties();

   public Serializable getPropertyValue( String group, String name )
   {
      return PropertyUtils.getPropertyValue( propertiesStore, group, name );
   }

   public static void loadProperties( HashMap<String, HashMap<String, Serializable>> propertiesStore, Object object ) throws IllegalArgumentException, IllegalAccessException
   {
      AnnotationScanner.forEachField( object.getClass(), PropertyMapping.class, AnnotationScanner.IS_NON_STATIC
               .and( AnnotationScanner.IS_NON_FINAL ), null, ( type, field, annotation ) -> PropertyUtils.setFieldValue( propertiesStore, object, field, (PropertyMapping) annotation ) );
   }

   public void loadProperties( Object... objects ) throws IllegalArgumentException, IllegalAccessException
   {
      for ( Object object : objects )
      {
         loadProperties( propertiesStore, object );
      }
   }

   public void loadProperties( HashMap<String, HashMap<String, Serializable>> propertiesStore, Class<?> classToLoadPropertiesWith ) throws IllegalArgumentException, IllegalAccessException
   {
      AnnotationScanner.forEachField( classToLoadPropertiesWith, PropertyMapping.class, AnnotationScanner.IS_STATIC
               .and( AnnotationScanner.IS_NON_FINAL ), null, ( type, field, annotation ) -> PropertyUtils.setFieldValue( propertiesStore, null, field, (PropertyMapping) annotation ) );
   }

   public void loadProperties( Class<?>... classesToLoadPropertiesWith ) throws IllegalArgumentException, IllegalAccessException
   {
      for ( Class<?> classToLoadPropertiesWith : classesToLoadPropertiesWith )
      {
         loadProperties( propertiesStore, classToLoadPropertiesWith );
      }
   }

   public static HashMap<String, Serializable> parseProperties( String propertiesDataString, DataFormat dataFormat ) throws JsonMappingException, JsonProcessingException
   {
      HashMap<String, Serializable> returnProperties = new HashMap<String, Serializable>();
      ParserUtils.readValue( dataFormat, propertiesDataString, ParserUtils.genericHashMapObjectType );
      return returnProperties;
   }

   public static HashMap<String, Serializable> parsePropertiesFile( String propertiesDataFile, DataFormat dataFormat ) throws IOException, URISyntaxException
   {
      return parseProperties( ClassPathLoaderUtils.readAllText( propertiesDataFile ), dataFormat );
   }

   public static HashMap<String, Serializable> parsePropertiesFile( String propertiesDataFile ) throws IOException, URISyntaxException
   {
      return parsePropertiesFile( propertiesDataFile, ParserUtils.getFileDataFormat( propertiesDataFile ) );
   }
}
