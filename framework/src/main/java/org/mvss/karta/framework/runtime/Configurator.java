package org.mvss.karta.framework.runtime;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.utils.ClassPathLoaderUtils;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Setter;

public class Configurator
{
   public static final TypeReference<HashMap<String, HashMap<String, Serializable>>> propertiesType                    = new TypeReference<HashMap<String, HashMap<String, Serializable>>>()
                                                                                                                       {
                                                                                                                       };

   private static ObjectMapper                                                       objectMapper                      = ParserUtils.getObjectMapper();

   private static HashMap<String, HashMap<String, Serializable>>                     propertiesStore                   = new HashMap<String, HashMap<String, Serializable>>();

   @Setter
   private static boolean                                                            useSystemProperties               = true;

   @Setter
   private static boolean                                                            useEnvironmentProperties          = true;

   @Setter
   private static boolean                                                            envPropertiesPreceedsOverSysProps = true;

   public static void MergeProperties( HashMap<String, HashMap<String, Serializable>> propertiesToMerge )
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

   public static void MergePropertiesString( String propertiesDataString ) throws IOException, URISyntaxException
   {
      HashMap<String, HashMap<String, Serializable>> propertiesToMerge = objectMapper.readValue( propertiesDataString, propertiesType );
      MergeProperties( propertiesToMerge );
   }

   public static void MergePropertiesFiles( String... propertyFiles ) throws IOException, URISyntaxException
   {
      for ( String propertyFile : propertyFiles )
      {
         MergePropertiesString( ClassPathLoaderUtils.readAllText( propertyFile ) );
      }
   }

   public static String getEnv( String key, String defaultValue )
   {
      String envValue = System.getenv( key );
      return ( envValue == null ) ? defaultValue : envValue;
   }

   public static Serializable getPropertyValue( String group, String name )
   {

      String propertyFromEnvOrSys = null;

      String keyForEnvOrSys = group + "." + name;
      String propFromEnv = System.getenv( keyForEnvOrSys );
      String propFromSys = System.getProperty( keyForEnvOrSys );

      if ( useEnvironmentProperties )
      {
         if ( envPropertiesPreceedsOverSysProps )
         {
            propertyFromEnvOrSys = ( useSystemProperties ) ? getEnv( keyForEnvOrSys, propFromSys ) : propFromEnv;
         }
         else
         {
            propertyFromEnvOrSys = ( useSystemProperties ) ? System.getProperty( keyForEnvOrSys, propFromEnv ) : propFromEnv;
         }
      }
      else
      {
         if ( useSystemProperties )
         {
            propertyFromEnvOrSys = propFromSys;
         }
      }

      if ( propertyFromEnvOrSys != null )
      {
         return objectMapper.convertValue( propertyFromEnvOrSys, Serializable.class );
      }

      HashMap<String, Serializable> groupStore = propertiesStore.get( group );
      return ( groupStore == null ) ? null : groupStore.get( name );
   }

   public static void setFieldValue( Object object, Field field ) throws IllegalArgumentException, IllegalAccessException
   {
      field.setAccessible( true );

      PropertyMapping propertyMapping = field.getDeclaredAnnotation( PropertyMapping.class );

      if ( propertyMapping != null )
      {
         String propertyGroup = propertyMapping.group();
         String propertyName = propertyMapping.propertyName();

         Serializable propertyValue = getPropertyValue( propertyGroup, propertyName );

         if ( propertyValue != null )
         {
            Class<?> covertToTypeTo = ( propertyMapping.type() == Object.class ) ? field.getType() : propertyMapping.type();

            if ( covertToTypeTo.isAssignableFrom( propertyValue.getClass() ) )
            {
               field.set( object, propertyValue );
            }
            else
            {
               field.set( object, objectMapper.convertValue( propertyValue, covertToTypeTo ) );
            }
         }
      }
   }

   public static void LoadProperties( Object... objects ) throws IllegalArgumentException, IllegalAccessException
   {
      for ( Object object : objects )
      {
         Class<?> theClassOfObject = object.getClass();

         if ( theClassOfObject == Object.class )
         {
            continue;
         }

         for ( Field fieldOfClass : theClassOfObject.getDeclaredFields() )
         {
            setFieldValue( object, fieldOfClass );
         }

         Class<?> superClassOfObject = theClassOfObject.getSuperclass();

         if ( superClassOfObject == Object.class )
         {
            continue;
         }

         LoadProperties( superClassOfObject.cast( object ) );
      }
   }

   public static void LoadStaticProperties( Class<?>... classesToLoadPropertiesWith ) throws IllegalArgumentException, IllegalAccessException
   {
      for ( Class<?> classToLoadPropertiesWith : classesToLoadPropertiesWith )
      {
         for ( Field fieldOfClass : classToLoadPropertiesWith.getDeclaredFields() )
         {
            if ( Modifier.isStatic( fieldOfClass.getModifiers() ) )
            {
               setFieldValue( null, fieldOfClass );
            }
         }
      }
   }

   public static HashMap<String, Serializable> parseProperties( String propertiesDataString ) throws JsonMappingException, JsonProcessingException
   {
      HashMap<String, Serializable> returnProperties = new HashMap<String, Serializable>();
      objectMapper.readValue( propertiesDataString, ParserUtils.genericHashMapObjectType );
      return returnProperties;
   }

   public static HashMap<String, Serializable> parsePropertiesFile( String propertiesDataFile ) throws IOException, URISyntaxException
   {
      return parseProperties( ClassPathLoaderUtils.readAllText( propertiesDataFile ) );
   }

}
