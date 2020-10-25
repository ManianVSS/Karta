package org.mvss.karta.framework.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertyUtils
{
   public static Pattern                 propertyPattern   = Pattern.compile( "\\$\\{([_A-Za-z0-9]+)\\}" );

   public static HashMap<String, String> systemPropertyMap = new HashMap<String, String>();

   static
   {
      System.getenv().forEach( ( key, value ) -> systemPropertyMap.put( key.toString().toUpperCase(), value.toString() ) );
      System.getProperties().forEach( ( key, value ) -> systemPropertyMap.put( key.toString(), value.toString() ) );
   }

   public static void expandEnvVars( Map<String, String> valueMap )
   {
      HashMap<String, String> expandedValue = new HashMap<String, String>();
      valueMap.forEach( ( key, value ) -> expandedValue.put( expandEnvVars( key ), PropertyUtils.expandEnvVars( value ) ) );
      valueMap.clear();
      valueMap.putAll( expandedValue );
   }

   public static void expandEnvVars( Collection<String> valueList )
   {
      ArrayList<String> expandedValue = new ArrayList<String>();
      valueList.forEach( ( value ) -> expandedValue.add( expandEnvVars( value ) ) );
      valueList.clear();
      valueList.addAll( expandedValue );
   }

   public static String expandEnvVars( String text )
   {
      if ( text == null )
      {
         return null;
      }
      return expandEnvVars( text, systemPropertyMap );
   }

   public static void mergeEnvValuesIntoMap( HashMap<String, String> propertyMap )
   {
      propertyMap.putAll( systemPropertyMap );
   }

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

   public static String getEnv( String key, String defaultValue )
   {
      String envValue = System.getenv( key );
      return ( envValue == null ) ? defaultValue : envValue;
   }

   public static String getSystemOrEnvProperty( String key, String defaultValue )
   {
      return systemPropertyMap.containsKey( key ) ? systemPropertyMap.get( key ) : defaultValue;
   }
}
