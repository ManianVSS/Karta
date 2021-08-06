package org.mvss.karta.framework.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.runtime.Constants;

/**
 * Utility class to work with generic maps, lists and other vectors
 * 
 * @author Manian
 */
public class DataUtils
{
   /**
    * Create a clone of a HashMap
    * 
    * @param <K>
    * @param <V>
    * @param source
    * @return
    */
   public static <K, V> HashMap<K, V> cloneMap( Map<K, V> source )
   {
      HashMap<K, V> clone = new HashMap<K, V>();

      if ( source != null )
      {
         clone.putAll( source );
      }
      return clone;
   }

   /**
    * Merge two lists into a new one
    * 
    * @param <K>
    * @param list1
    * @param list2
    * @return
    */
   public static <K> ArrayList<K> mergeLists( Collection<K> list1, Collection<K> list2 )
   {
      ArrayList<K> mergedList = new ArrayList<K>();
      if ( list1 != null )
      {
         mergedList.addAll( list1 );
      }
      if ( list2 != null )
      {
         mergedList.addAll( list2 );
      }
      return mergedList;
   }

   /**
    * Adds items from source list to destination list if missing
    * 
    * @param <K>
    * @param destination
    * @param source
    */
   public static <K> void addMissing( Collection<K> destination, Collection<K> source )
   {
      if ( destination != null )
      {
         if ( source != null )
         {
            for ( K item : source )
            {
               if ( !destination.contains( item ) )
               {
                  destination.add( item );
               }
            }
         }
      }
   }

   /**
    * Merges two Maps into a new HashMap
    * 
    * @param <K>
    * @param <V>
    * @param map1
    * @param map2
    * @return
    */
   public static <K, V> HashMap<K, V> mergeMaps( Map<K, V> map1, Map<K, V> map2 )
   {
      HashMap<K, V> mergedMap = new HashMap<K, V>();
      if ( map1 != null )
      {
         map1.forEach( ( key, value ) -> mergedMap.put( key, value ) );
      }

      if ( map2 != null )
      {
         map2.forEach( ( key, value ) -> mergedMap.put( key, value ) );
      }
      return mergedMap;
   }

   /**
    * Merges entries from source map into destination map with override.
    * 
    * @param <K>
    * @param <V>
    * @param sourceVars
    * @param destinationVars
    */
   public static <K, V> void mergeMapInto( Map<K, V> sourceVars, Map<K, V> destinationVars )
   {
      if ( ( sourceVars != null ) && ( sourceVars != destinationVars ) )
      {
         for ( K variableName : sourceVars.keySet() )
         {
            destinationVars.put( variableName, sourceVars.get( variableName ) );
         }
      }
   }

   /**
    * Adds an item to a tree-map with in sequence with the Integer sequence action as the tree-map key and value being the list of items for the sequence
    * 
    * @param <T>
    * @param item
    * @param map
    * @param sequence
    */
   public static <T> void addItemToTreeMapInSequence( T item, TreeMap<Integer, ArrayList<T>> map, Integer sequence )
   {
      if ( ( map == null ) || ( item == null ) )
      {
         return;
      }

      if ( sequence == null )
      {
         sequence = Integer.MAX_VALUE;
      }

      if ( !map.containsKey( sequence ) )
      {
         map.put( sequence, new ArrayList<T>() );
      }

      map.get( sequence ).add( item );
   }

   /**
    * Serialize a tree-map of sequence to list to a single list
    * 
    * @param <T>
    * @param itemSequenceMap
    * @return
    */
   public static <T> ArrayList<T> generateSequencedList( TreeMap<Integer, ArrayList<T>> itemSequenceMap )
   {
      ArrayList<T> itemSequence = new ArrayList<T>();

      if ( itemSequenceMap.containsKey( 0 ) )
      {
         itemSequenceMap.put( itemSequenceMap.size(), itemSequenceMap.remove( 0 ) );
      }

      for ( ArrayList<T> itemsOfSequence : itemSequenceMap.values() )
      {
         itemSequence.addAll( itemsOfSequence );
      }

      return itemSequence;
   }

   /**
    * Validates if a given value is in range of min to max.
    * Inclusion of min and max values are configured by parameters.
    * 
    * @param value
    * @param min
    * @param max
    * @param includeMin
    * @param includeMax
    * @return
    */
   public static boolean inRange( byte value, byte min, byte max, boolean includeMin, boolean includeMax )
   {
      return ( includeMin ? ( value >= min ) : ( value > min ) ) && ( includeMax ? ( value <= max ) : ( value < max ) );
   }

   /**
    * Validates if a given value is in range of min to max.
    * Inclusion of min and max values are configured by parameters.
    * 
    * @param value
    * @param min
    * @param max
    * @param includeMin
    * @param includeMax
    * @return
    */
   public static boolean inRange( int value, int min, int max, boolean includeMin, boolean includeMax )
   {
      return ( includeMin ? ( value >= min ) : ( value > min ) ) && ( includeMax ? ( value <= max ) : ( value < max ) );
   }

   /**
    * Validates if a given value is in range of min to max.
    * Inclusion of min and max values are configured by parameters.
    * 
    * @param value
    * @param min
    * @param max
    * @param includeMin
    * @param includeMax
    * @return
    */

   public static boolean inRange( long value, long min, long max, boolean includeMin, boolean includeMax )
   {
      return ( includeMin ? ( value >= min ) : ( value > min ) ) && ( includeMax ? ( value <= max ) : ( value < max ) );
   }

   /**
    * Validates if a given value is in range of min to max.
    * Inclusion of min and max values are configured by parameters.
    * 
    * @param value
    * @param min
    * @param max
    * @param includeMin
    * @param includeMax
    * @return
    */
   public static boolean inRange( float value, float min, float max, boolean includeMin, boolean includeMax )
   {
      return ( includeMin ? ( value >= min ) : ( value > min ) ) && ( includeMax ? ( value <= max ) : ( value < max ) );
   }

   /**
    * Validates if a given value is in range of min to max.
    * Inclusion of min and max values are configured by parameters.
    * 
    * @param value
    * @param min
    * @param max
    * @param includeMin
    * @param includeMax
    * @return
    */
   public static boolean inRange( double value, double min, double max, boolean includeMin, boolean includeMax )
   {
      return ( includeMin ? ( value >= min ) : ( value > min ) ) && ( includeMax ? ( value <= max ) : ( value < max ) );
   }

   /**
    * Validates if a given value is in range of min to max both included.
    * 
    * @param value
    * @param min
    * @param max
    * @return
    */
   public static boolean inRange( byte value, byte min, byte max )
   {
      return inRange( value, min, max, true, true );
   }

   /**
    * Validates if a given value is in range of min to max both included.
    * 
    * @param value
    * @param min
    * @param max
    * @return
    */
   public static boolean inRange( int value, int min, int max )
   {
      return inRange( value, min, max, true, true );
   }

   /**
    * Validates if a given value is in range of min to max.
    * Inclusion of min and max values are configured by parameters.
    * 
    * @param value
    * @param min
    * @param max
    * @return
    */
   public static boolean inRange( long value, long min, long max )
   {
      return inRange( value, min, max, true, true );
   }

   /**
    * Validates if a given value is in range of min to max both included.
    * 
    * @param value
    * @param min
    * @param max
    * @return
    */
   public static boolean inRange( float value, float min, float max )
   {
      return inRange( value, min, max, true, true );
   }

   /**
    * Validates if a given value is in range of min to max both included.
    * 
    * @param value
    * @param min
    * @param max
    * @return
    */
   public static boolean inRange( double value, double min, double max )
   {
      return inRange( value, min, max, true, true );
   }

   /**
    * Converts a Serializable object of types Integer/String to int value
    * 
    * @param serializable
    * @param defaultValue
    * @return
    */
   public static int serializableToInteger( Serializable serializable, int defaultValue )
   {
      int returnValue = defaultValue;

      try
      {
         if ( serializable != null )
         {
            if ( serializable instanceof Integer )
            {
               returnValue = Integer.valueOf( (Integer) serializable );
            }

            if ( serializable instanceof String )
            {
               returnValue = Integer.valueOf( (String) serializable );
            }
         }
      }
      catch ( Throwable t )
      {

      }

      return returnValue;
   }

   /**
    * Converts a Serializable object of types Long/Integer/String to long value
    * 
    * @param serializable
    * @param defaultValue
    * @return
    */
   public static long serializableToLong( Serializable serializable, long defaultValue )
   {
      long returnValue = defaultValue;

      try
      {
         if ( serializable != null )
         {
            if ( serializable instanceof Long )
            {
               returnValue = Long.valueOf( (Long) serializable );
            }

            if ( serializable instanceof Integer )
            {
               returnValue = Long.valueOf( (Integer) serializable );
            }

            if ( serializable instanceof String )
            {
               returnValue = Long.valueOf( (String) serializable );
            }
         }
      }
      catch ( Throwable t )
      {

      }

      return returnValue;
   }

   /**
    * Returns an string incremented by one.
    * Only alphanumeric digits are considered for incrementing and carry forward.
    * Digits are assumed to have most significant digit from left to least significant digit to right (natural ordering).
    * e.g. "aZ9_z$z" is incremented to "bA0_a$a"
    * 
    * @param currentString
    * @return
    */
   public static String incrementStringAlphaNumerically( String currentString )
   {
      char[] chararray = currentString.toCharArray();

      int carry = 1;
      for ( int j = chararray.length - 1; j >= 0; j-- )
      {
         if ( carry > 0 )
         {
            if ( ( chararray[j] >= 'a' ) && ( chararray[j] <= 'z' ) )
            {
               if ( chararray[j] != 'z' )
               {
                  chararray[j]++;
                  carry = 0;
                  break;
               }
               else
               {
                  chararray[j] = 'a';
                  carry = 1;
               }
            }
            else if ( ( chararray[j] >= 'A' ) && ( chararray[j] <= 'Z' ) )
            {
               if ( chararray[j] != 'Z' )
               {
                  chararray[j]++;
                  carry = 0;
                  break;
               }
               else
               {
                  chararray[j] = 'A';
                  carry = 1;
               }
            }
            else if ( ( chararray[j] >= '0' ) && ( chararray[j] <= '9' ) )
            {
               if ( chararray[j] != '9' )
               {
                  chararray[j]++;
                  carry = 0;
                  break;
               }
               else
               {
                  chararray[j] = '0';
                  carry = 1;
               }
            }
            else
            {
               continue;
            }
         }
      }
      return String.valueOf( chararray );
   }

   public static double round2( double value )
   {
      return Math.round( value * 100 ) / 100.0;
   }

   public static <T> T pick( Predicate<T> condition, T[] values )
   {
      if ( values == null )
      {
         return null;
      }

      for ( T value : values )
      {
         if ( condition.test( value ) )
         {
            return value;
         }
      }

      return values[values.length - 1];
   }

   public static String pickString( Predicate<String> condition, String... strings )
   {
      return pick( condition, strings );
   }

   public static String pickNonNull( String... strings )
   {
      return pick( ( str ) -> ( str != null ), strings );
   }

   public Object[] getMergedNonNullValues( Object... values )
   {
      List<Object> returnValue = Arrays.asList( values );
      returnValue.removeIf( ( value ) -> ( value == null ) );
      return returnValue.toArray();
   }

   public static String constructURL( String... urlPieces )
   {
      String fullURL = Constants.EMPTY_STRING;

      if ( urlPieces == null )
      {
         return null;
      }

      for ( int i = 0; i < urlPieces.length; i++ )
      {
         if ( StringUtils.isNotBlank( urlPieces[i] ) )
         {
            fullURL = StringUtils.strip( fullURL + Constants.SLASH + StringUtils.strip( urlPieces[i], Constants.SLASH ), Constants.SLASH );
         }
      }

      return fullURL;
   }
}
