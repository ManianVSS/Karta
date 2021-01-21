package org.mvss.karta.framework.utils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

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
   public static Pattern                 propertyPattern   = Pattern.compile( "\\$\\{([_A-Za-z0-9]+)\\}" );

   /**
    * Cached environment and system properties with system properties having higher precedence
    */
   public static HashMap<String, String> systemPropertyMap = new HashMap<String, String>();

   private static ObjectMapper           objectMapper      = ParserUtils.getObjectMapper();
   private static ObjectMapper           yamlObjectMapper  = ParserUtils.getYamlObjectMapper();

   static
   {
      System.getenv().forEach( ( key, value ) -> systemPropertyMap.put( key.toString().toUpperCase(), value.toString() ) );
      System.getProperties().forEach( ( key, value ) -> systemPropertyMap.put( key.toString(), value.toString() ) );
   }

   /**
    * Expands system properties in format ${propertyName} in keys of the map
    * 
    * @param <V>
    * @param valueMap
    */
   public static <V> void expandEnvVarsForMap( Map<String, V> valueMap )
   {
      HashMap<String, V> expandedValue = new HashMap<String, V>();
      valueMap.forEach( ( key, value ) -> expandedValue.put( expandEnvVars( key ), value ) );
      valueMap.clear();
      valueMap.putAll( expandedValue );
   }

   /**
    * Expands system properties in format ${propertyName} in keys and values of the map
    * 
    * @param valueMap
    */
   public static void expandEnvVars( Map<String, String> valueMap )
   {
      HashMap<String, String> expandedValue = new HashMap<String, String>();
      valueMap.forEach( ( key, value ) -> expandedValue.put( expandEnvVars( key ), PropertyUtils.expandEnvVars( value ) ) );
      valueMap.clear();
      valueMap.putAll( expandedValue );
   }

   /**
    * Expands system properties in format ${propertyName} in the collection values
    * 
    * @param valueList
    */
   public static void expandEnvVars( Collection<String> valueList )
   {
      ArrayList<String> expandedValue = new ArrayList<String>();
      valueList.forEach( ( value ) -> expandedValue.add( expandEnvVars( value ) ) );
      valueList.clear();
      valueList.addAll( expandedValue );
   }

   /**
    * Expands system properties in format ${propertyName} in the string text.
    * 
    * @param text
    * @return
    */
   public static String expandEnvVars( String text )
   {
      if ( text == null )
      {
         return null;
      }
      return expandEnvVars( text, systemPropertyMap );
   }

   /**
    * Merge system properties into the property map
    * 
    * @param propertyMap
    */
   public static void mergeEnvValuesIntoMap( HashMap<String, String> propertyMap )
   {
      propertyMap.putAll( systemPropertyMap );
   }

   /**
    * Expand system properties in a string with a given system properites map
    * 
    * @param text
    * @param systemPropertyMap
    * @return
    */
   public static String expandEnvVars( String text, HashMap<String, String> systemPropertyMap )
   {
      if ( text == null )
      {
         return null;
      }

      HashMap<String, String> newMap = systemPropertyMap;

      if ( systemPropertyMap != PropertyUtils.systemPropertyMap )
      {
         newMap = new HashMap<String, String>();
         newMap.putAll( systemPropertyMap );
         newMap.putAll( PropertyUtils.systemPropertyMap );
      }

      boolean found = false;
      do
      {
         found = false;
         Matcher matcher = propertyPattern.matcher( text );

         while ( matcher.find() )
         {
            String propValue = systemPropertyMap.get( matcher.group( 1 ).toUpperCase() );
            if ( propValue != null )
            {
               found = true;
               propValue = propValue.replace( "\\", "\\\\" );
               Pattern subexpr = Pattern.compile( Pattern.quote( matcher.group( 0 ) ) );
               text = subexpr.matcher( text ).replaceAll( propValue );
            }
         }
      }
      while ( found == true );

      return text;
   }

   /**
    * Returns environmental variable returning default value for undefined keys
    * 
    * @param key
    * @param defaultValue
    * @return
    */
   public static String getEnv( String key, String defaultValue )
   {
      String envValue = System.getenv( key );
      return ( envValue == null ) ? defaultValue : envValue;
   }

   /**
    * Returns system/env property returning default value for undefined keys
    * 
    * @param key
    * @param defaultValue
    * @return
    */
   public static String getSystemOrEnvProperty( String key, String defaultValue )
   {
      return systemPropertyMap.containsKey( key ) ? systemPropertyMap.get( key ) : defaultValue;
   }

   /**
    * Sets the value of a object's field based on the field type converting from the serializable value (property matched)
    * 
    * @param object
    * @param field
    * @param propertyValue
    * @param castAsType
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
            else
            {
               field.set( object, objectMapper.convertValue( propertyValue, castAsType ) );
            }
         }
      }
      catch ( Throwable t )
      {
         log.error( Constants.EMPTY_STRING, t );
      }
   }

   /**
    * Get property value from a property store giving precedence to system/environment property.
    * Useful in Kubernetes/Docker environment with environment variable acting as container parameters.
    * 
    * @param propertiesStore
    * @param group
    * @param name
    * @return
    */
   public static Serializable getPropertyValue( HashMap<String, HashMap<String, Serializable>> propertiesStore, String group, String name )
   {
      String keyForEnvOrSys = group + "." + name;
      String propertyFromEnvOrSys = systemPropertyMap.get( keyForEnvOrSys );

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
    * 
    * @param propertiesStore
    * @param object
    * @param field
    * @param propertyMapping
    */
   public static void setFieldValue( HashMap<String, HashMap<String, Serializable>> propertiesStore, Object object, Field field, PropertyMapping propertyMapping )
   {
      try
      {
         String propertyGroup = propertyMapping.group();
         String propertyName = propertyMapping.value();

         if ( StringUtils.isEmpty( propertyName ) )
         {
            propertyName = field.getName();
         }

         Serializable propertyValue = getPropertyValue( propertiesStore, propertyGroup, propertyName );
         Class<?> covertToTypeTo = ( Object.class == propertyMapping.type() ) ? field.getType() : propertyMapping.type();
         PropertyUtils.setFieldValue( object, field, propertyValue, covertToTypeTo );
      }
      catch ( Throwable t )
      {
         log.error( Constants.EMPTY_STRING, t );
      }
   }
}
