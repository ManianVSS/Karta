package org.mvss.karta.framework.runtime;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;

import org.mvss.karta.framework.core.KartaAutoWired;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.utils.ClassPathLoaderUtils;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

public class Configurator
{
   public static final TypeReference<HashMap<String, HashMap<String, Serializable>>> propertiesType                    = new TypeReference<HashMap<String, HashMap<String, Serializable>>>()
                                                                                                                       {
                                                                                                                       };

   private static ObjectMapper                                                       objectMapper                      = ParserUtils.getObjectMapper();
   private static ObjectMapper                                                       yamlObjectMapper                  = ParserUtils.getYamlObjectMapper();

   @Getter
   private HashMap<String, HashMap<String, Serializable>>                            propertiesStore                   = new HashMap<String, HashMap<String, Serializable>>();

   @Setter
   private static boolean                                                            useSystemProperties               = true;

   @Setter
   private static boolean                                                            useEnvironmentProperties          = true;

   @Setter
   private static boolean                                                            envPropertiesPreceedsOverSysProps = true;

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

   public static String getEnv( String key, String defaultValue )
   {
      String envValue = System.getenv( key );
      return ( envValue == null ) ? defaultValue : envValue;
   }

   public static Serializable getPropertyValue( HashMap<String, HashMap<String, Serializable>> propertiesStore, String group, String name )
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
         return yamlObjectMapper.convertValue( propertyFromEnvOrSys, Serializable.class );
      }

      HashMap<String, Serializable> groupStore = propertiesStore.get( group );
      return ( groupStore == null ) ? null : groupStore.get( name );
   }

   public Serializable getPropertyValue( String group, String name )
   {
      return getPropertyValue( propertiesStore, group, name );
   }

   public static void setFieldValue( HashMap<String, HashMap<String, Serializable>> propertiesStore, Object object, Field field ) throws IllegalArgumentException, IllegalAccessException
   {
      field.setAccessible( true );

      PropertyMapping propertyMapping = field.getDeclaredAnnotation( PropertyMapping.class );

      if ( propertyMapping != null )
      {
         // TODO: Default property name to field name if blank
         String propertyGroup = propertyMapping.group();
         String propertyName = propertyMapping.value();

         Serializable propertyValue = getPropertyValue( propertiesStore, propertyGroup, propertyName );

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

   public void setFieldValue( Object object, Field field ) throws IllegalArgumentException, IllegalAccessException
   {
      setFieldValue( propertiesStore, object, field );
   }

   public static void loadProperties( HashMap<String, HashMap<String, Serializable>> propertiesStore, Object object ) throws IllegalArgumentException, IllegalAccessException
   {
      loadProperties( propertiesStore, object, object.getClass() );
   }

   public static void loadProperties( HashMap<String, HashMap<String, Serializable>> propertiesStore, Object object, Class<?> theClassOfObject ) throws IllegalArgumentException, IllegalAccessException
   {
      if ( ( object == null ) || ( theClassOfObject == null ) || theClassOfObject.getName().equals( Object.class.getName() ) || !theClassOfObject.isAssignableFrom( object.getClass() ) )
      {
         return;
      }

      for ( Field fieldOfClass : theClassOfObject.getDeclaredFields() )
      {
         setFieldValue( propertiesStore, object, fieldOfClass );
      }

      Class<?> superClass = theClassOfObject.getSuperclass();
      if ( superClass.getName().equals( Object.class.getName() ) )
      {
         return;
      }
      else
      {
         loadProperties( propertiesStore, object, superClass );
      }
   }

   public static void loadBeans( Object object, HashSet<Object> beans ) throws IllegalArgumentException, IllegalAccessException
   {
      loadBeans( object, object.getClass(), beans );
   }

   public static void loadBeans( Object object, Class<?> theClassOfObject, HashSet<Object> beans ) throws IllegalArgumentException, IllegalAccessException
   {
      if ( ( object == null ) || ( theClassOfObject == null ) || theClassOfObject.getName().equals( Object.class.getName() ) || !theClassOfObject.isAssignableFrom( object.getClass() ) )
      {
         return;
      }
      for ( Field field : theClassOfObject.getDeclaredFields() )
      {
         field.setAccessible( true );

         KartaAutoWired propertyMapping = field.getDeclaredAnnotation( KartaAutoWired.class );

         if ( propertyMapping != null )
         {
            Class<?> fieldClass = field.getType();

            for ( Object bean : beans )
            {
               if ( fieldClass == bean.getClass() )
               {
                  field.set( object, bean );
                  break;
               }
            }
         }
      }

      Class<?> superClass = theClassOfObject.getSuperclass();
      if ( superClass.getName().equals( Object.class.getName() ) )
      {
         return;
      }
      else
      {
         loadBeans( object, superClass, beans );
      }
   }

   public void loadProperties( Object... objects ) throws IllegalArgumentException, IllegalAccessException
   {
      for ( Object object : objects )
      {
         loadProperties( propertiesStore, object );
      }
   }

   public void loadStaticProperties( HashMap<String, HashMap<String, Serializable>> propertiesStore, Class<?> classToLoadPropertiesWith ) throws IllegalArgumentException, IllegalAccessException
   {
      for ( Field fieldOfClass : classToLoadPropertiesWith.getDeclaredFields() )
      {
         if ( Modifier.isStatic( fieldOfClass.getModifiers() ) )
         {
            setFieldValue( null, fieldOfClass );
         }
      }
   }

   public void loadStaticProperties( Class<?>... classesToLoadPropertiesWith ) throws IllegalArgumentException, IllegalAccessException
   {
      for ( Class<?> classToLoadPropertiesWith : classesToLoadPropertiesWith )
      {
         loadStaticProperties( propertiesStore, classToLoadPropertiesWith );
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
