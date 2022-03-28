package org.mvss.karta.framework.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to load bean properties from property files (YAML, JSON or XML)
 *
 * @author Manian
 */
@Log4j2
public class PropertyUtils
{

   /**
    * The compiled regex patter for matching property references in format ${propertyName}
    */
   public static Pattern propertyPattern = Pattern.compile( "\\$\\{([_A-Za-z0-9]+)\\}" );

   /**
    * Cached environment and system properties with system properties having higher precedence
    */
   public final static HashMap<String, String> systemPropertyMap = new HashMap<>();

   private static final ObjectMapper objectMapper     = ParserUtils.getObjectMapper();
   private static final ObjectMapper yamlObjectMapper = ParserUtils.getYamlObjectMapper();

   static
   {
      System.getenv().forEach( ( key, value ) -> systemPropertyMap.put( key.toUpperCase(), value ) );
      System.getProperties().forEach( ( key, value ) -> systemPropertyMap.put( key.toString(), value.toString() ) );
   }

   /**
    * Expands system properties in format ${propertyName} in keys of the map
    */
   public static <V> void expandEnvVarsForMap( Map<String, V> valueMap )
   {
      HashMap<String, V> expandedValue = new HashMap<>();
      valueMap.forEach( ( key, value ) -> expandedValue.put( expandEnvVars( key ), value ) );
      valueMap.clear();
      valueMap.putAll( expandedValue );
   }

   /**
    * Expands system properties in format ${propertyName} in keys and values of the map
    */
   public static void expandEnvVars( Map<String, String> valueMap )
   {
      HashMap<String, String> expandedValue = new HashMap<>();
      valueMap.forEach( ( key, value ) -> expandedValue.put( expandEnvVars( key ), PropertyUtils.expandEnvVars( value ) ) );
      valueMap.clear();
      valueMap.putAll( expandedValue );
   }

   /**
    * Expands system properties in format ${propertyName} in the collection values
    */
   public static void expandEnvVars( Collection<String> valueList )
   {
      ArrayList<String> expandedValue = new ArrayList<>();
      valueList.forEach( ( value ) -> expandedValue.add( expandEnvVars( value ) ) );
      valueList.clear();
      valueList.addAll( expandedValue );
   }

   /**
    * Expands system properties in format ${propertyName} in the string text.
    */
   public static String expandEnvVars( String text )
   {
      if ( text == null )
      {
         return null;
      }
      return expandPropertiesIntoText( systemPropertyMap, text );
   }

   /**
    * Merge system properties into the property map
    */
   public static void mergeEnvValuesIntoMap( HashMap<String, String> propertyMap )
   {
      if ( propertyMap != PropertyUtils.systemPropertyMap )
      {
         propertyMap.putAll( systemPropertyMap );
      }
   }

   /**
    * Expand system properties in a string with a given properties map
    */
   public static String expandPropertiesIntoText( HashMap<String, String> propertyMap, String text )
   {
      if ( text == null )
      {
         return null;
      }

      mergeEnvValuesIntoMap( propertyMap );

      boolean found;
      do
      {
         found = false;
         Matcher matcher = propertyPattern.matcher( text );

         while ( matcher.find() )
         {
            String propValue = propertyMap.get( matcher.group( 1 ).toUpperCase() );
            if ( propValue != null )
            {
               found     = true;
               propValue = propValue.replace( Constants.BACKSLASH, Constants.DOUBLE_BACKSLASH );
               Pattern subExpression = Pattern.compile( Pattern.quote( matcher.group( 0 ) ) );
               text = subExpression.matcher( text ).replaceAll( propValue );
            }
         }
      }
      while ( found );

      return text;
   }

   /**
    * Converts a property store into a properties map which can be used to substitute values
    */
   public static HashMap<String, String> convertToPropertyMap( HashMap<String, HashMap<String, Serializable>> propertiesStore )
   {
      HashMap<String, String> propertyMap = new HashMap<>();

      for ( Entry<String, HashMap<String, Serializable>> propertyStoreEntry : propertiesStore.entrySet() )
      {
         for ( Entry<String, Serializable> properties : propertyStoreEntry.getValue().entrySet() )
         {
            try
            {
               propertyMap.put( propertyStoreEntry.getKey() + Constants.DOT + properties.getKey(),
                        objectMapper.writeValueAsString( properties.getValue() ) );
            }
            catch ( JsonProcessingException e )
            {
               propertyMap.put( propertyStoreEntry.getKey() + Constants.DOT + properties.getKey(), properties.getValue().toString() );
            }
         }
      }

      mergeEnvValuesIntoMap( propertyMap );

      return propertyMap;
   }

   public static String expandPropertiesStoreIntoText( HashMap<String, HashMap<String, Serializable>> propertiesStore, String text )
   {
      return expandPropertiesIntoText( convertToPropertyMap( propertiesStore ), text );
   }

   /**
    * Returns environmental variable returning default value for undefined keys
    */
   public static String getEnv( String key, String defaultValue )
   {
      String envValue = System.getenv( key );
      return ( envValue == null ) ? defaultValue : envValue;
   }

   /**
    * Returns system/env property returning default value for undefined keys
    */
   public static String getSystemOrEnvProperty( String key, String defaultValue )
   {
      return systemPropertyMap.getOrDefault( key, defaultValue );
   }

   /**
    * Sets the value of an object's field based on the field type converting from the serializable value (property matched)
    */
   public static void setFieldValue( Object object, Field field, Serializable propertyValue, Class<?> castAsType )
   {
      try
      {
         field.setAccessible( true );

         if ( propertyValue != null )
         {
            if ( castAsType == null )
            {
               castAsType = field.getType();
            }

            if ( castAsType.isAssignableFrom( propertyValue.getClass() ) )
            {
               field.set( object, propertyValue );
            }
            else if ( ( castAsType == Pattern.class ) && ( propertyValue.getClass() == String.class ) )
            {
               field.set( object, Pattern.compile( (String) propertyValue ) );
            }
            else
            {
               field.set( object, objectMapper.convertValue( propertyValue, castAsType ) );
            }
         }
      }
      catch ( Throwable t )
      {
         log.error( "Error setting field: " + field + " value: " + propertyValue + " for object: " + object, t );
      }
   }

   /**
    * Sets the value of an object's field based on the field type converting from the serializable value (property matched)
    */
   public static void setFieldValue( Object object, Field field, Serializable propertyValue, JavaType castAsType )
   {
      try
      {
         field.setAccessible( true );

         if ( propertyValue != null )
         {
            if ( castAsType == null )
            {
               castAsType = objectMapper.getTypeFactory().constructType( field.getType() );
            }

            if ( ( castAsType.getRawClass() == Pattern.class ) && ( propertyValue.getClass() == String.class ) )
            {
               field.set( object, Pattern.compile( (String) propertyValue ) );
            }
            else
            {
               field.set( object, objectMapper.convertValue( propertyValue, castAsType ) );
            }
         }
      }
      catch ( Throwable t )
      {
         log.error( "Error setting field: " + field + " value: " + propertyValue + " for object: " + object, t );
      }
   }

   /**
    * Get property value from a property store giving precedence to system/environment property.
    * Useful in Kubernetes/Docker environment with environment variable acting as container parameters.
    */
   public static Serializable getPropertyValue( HashMap<String, HashMap<String, Serializable>> propertiesStore, String group, String name )
   {
      String keyForEnvOrSys       = group + Constants.UNDERSCORE + name;
      String propertyFromEnvOrSys = systemPropertyMap.get( keyForEnvOrSys.toUpperCase() );

      if ( propertyFromEnvOrSys != null )
      {
         return yamlObjectMapper.convertValue( propertyFromEnvOrSys, Serializable.class );
      }

      HashMap<String, Serializable> groupStore = propertiesStore.get( group );
      return ( groupStore == null ) ? null : groupStore.get( name );
   }

   /**
    * Sets the value of an object's field by matching property from a property store based on PropertyMapping annotation
    * Note: A property store is a HashMap of property group name to HashMap of property names and values for the group.
    */
   public static void setFieldValue( HashMap<String, HashMap<String, Serializable>> propertiesStore, Object object, Field field,
                                     PropertyMapping propertyMapping )
   {
      try
      {
         String propertyGroup = propertyMapping.group();
         String propertyName  = DataUtils.pickString( StringUtils::isNotEmpty, propertyMapping.name(), propertyMapping.value(), field.getName() );

         Serializable propertyValue = getPropertyValue( propertiesStore, propertyGroup, propertyName );
         JavaType covertToTypeTo = objectMapper.getTypeFactory()
                  .constructType( ( Object.class == propertyMapping.type() ) ? field.getGenericType() : propertyMapping.type() );
         PropertyUtils.setFieldValue( object, field, propertyValue, covertToTypeTo );
      }
      catch ( Throwable t )
      {
         log.error( "Error setting field: " + field + " value for object: " + object, t );
      }
   }

   public static String evaluatePropertyValue( String key, Properties properties, String defaultValue )
   {
      return System.getProperty( key, getEnv( key, ( properties == null ) ? defaultValue : properties.getProperty( key, defaultValue ) ) );
   }

   public static void updatePropertyFileFromEnvironment( String propertyFileName ) throws IOException
   {
      File propertyFile = new File( propertyFileName );

      Properties properties = new Properties();

      if ( !propertyFile.exists() )
      {
         InputStream is = PropertyUtils.class.getResourceAsStream( Constants.SLASH + propertyFileName );
         if ( is == null )
         {
            throw new IOException( "Properties file:" + propertyFileName + " could not found." );
         }
         try (is)
         {
            properties.load( is );
         }
      }
      else
      {
         try (FileInputStream fis = new FileInputStream( propertyFile ))
         {
            properties.load( fis );
         }
      }

      for ( Object key : properties.keySet() )
      {
         String keyStr = (String) key;

         properties.setProperty( keyStr, evaluatePropertyValue( keyStr, properties, null ) );
      }

      try (FileOutputStream fos = new FileOutputStream( propertyFile ))
      {
         properties.store( fos, "Modified env properties after loading from environment" );
      }
   }
}
