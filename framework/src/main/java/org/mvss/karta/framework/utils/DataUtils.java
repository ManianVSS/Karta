package org.mvss.karta.framework.utils;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.runtime.Constants;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;

/**
 * Utility class to work with generic maps, lists and other vectors
 *
 * @author Manian
 */
public class DataUtils
{
   /**
    * Create a clone of a HashMap
    */
   public static <K, V> HashMap<K, V> cloneMap( Map<K, V> source )
   {
      HashMap<K, V> clone = new HashMap<>();

      if ( source != null )
      {
         clone.putAll( source );
      }
      return clone;
   }

   /**
    * Merge two lists into a new one
    */
   public static <K> ArrayList<K> mergeLists( Collection<K> list1, Collection<K> list2 )
   {
      ArrayList<K> mergedList = new ArrayList<>();
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
    */
   public static <K, V> HashMap<K, V> mergeMaps( Map<K, V> map1, Map<K, V> map2 )
   {
      HashMap<K, V> mergedMap = new HashMap<>();
      if ( map1 != null )
      {
         mergedMap.putAll( map1 );
      }

      if ( map2 != null )
      {
         mergedMap.putAll( map2 );
      }
      return mergedMap;
   }

   /**
    * Merges entries from source map into destination map with override.
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
         map.put( sequence, new ArrayList<>() );
      }

      map.get( sequence ).add( item );
   }

   /**
    * Serialize a tree-map of sequence to list to a single list
    */
   public static <T> ArrayList<T> generateSequencedList( TreeMap<Integer, ArrayList<T>> itemSequenceMap )
   {
      ArrayList<T> itemSequence = new ArrayList<>();

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
    */
   public static boolean inRange( byte value, byte min, byte max, boolean includeMin, boolean includeMax )
   {
      return ( includeMin ? ( value >= min ) : ( value > min ) ) && ( includeMax ? ( value <= max ) : ( value < max ) );
   }

   /**
    * Validates if a given value is in range of min to max.
    * Inclusion of min and max values are configured by parameters.
    */
   public static boolean inRange( int value, int min, int max, boolean includeMin, boolean includeMax )
   {
      return ( includeMin ? ( value >= min ) : ( value > min ) ) && ( includeMax ? ( value <= max ) : ( value < max ) );
   }

   /**
    * Validates if a given value is in range of min to max.
    * Inclusion of min and max values are configured by parameters.
    */

   public static boolean inRange( long value, long min, long max, boolean includeMin, boolean includeMax )
   {
      return ( includeMin ? ( value >= min ) : ( value > min ) ) && ( includeMax ? ( value <= max ) : ( value < max ) );
   }

   /**
    * Validates if a given value is in range of min to max.
    * Inclusion of min and max values are configured by parameters.
    */
   public static boolean inRange( float value, float min, float max, boolean includeMin, boolean includeMax )
   {
      return ( includeMin ? ( value >= min ) : ( value > min ) ) && ( includeMax ? ( value <= max ) : ( value < max ) );
   }

   /**
    * Validates if a given value is in range of min to max.
    * Inclusion of min and max values are configured by parameters.
    */
   public static boolean inRange( double value, double min, double max, boolean includeMin, boolean includeMax )
   {
      return ( includeMin ? ( value >= min ) : ( value > min ) ) && ( includeMax ? ( value <= max ) : ( value < max ) );
   }

   /**
    * Validates if a given value is in range of min to max both included.
    */
   public static boolean inRange( byte value, byte min, byte max )
   {
      return inRange( value, min, max, true, true );
   }

   /**
    * Validates if a given value is in range of min to max both included.
    */
   public static boolean inRange( int value, int min, int max )
   {
      return inRange( value, min, max, true, true );
   }

   /**
    * Validates if a given value is in range of min to max.
    * Inclusion of min and max values are configured by parameters.
    */
   public static boolean inRange( long value, long min, long max )
   {
      return inRange( value, min, max, true, true );
   }

   /**
    * Validates if a given value is in range of min to max both included.
    */
   public static boolean inRange( float value, float min, float max )
   {
      return inRange( value, min, max, true, true );
   }

   /**
    * Validates if a given value is in range of min to max both included.
    */
   public static boolean inRange( double value, double min, double max )
   {
      return inRange( value, min, max, true, true );
   }

   /**
    * Converts a Serializable object of types Integer/String to int value
    */
   public static int serializableToInteger( Serializable serializable, int defaultValue )
   {
      try
      {
         if ( serializable != null )
         {
            if ( serializable instanceof Integer )
            {
               return (Integer) serializable;
            }
            if ( serializable instanceof String )
            {
               return Integer.parseInt( (String) serializable );
            }
         }
      }
      catch ( Throwable t )
      {
         return defaultValue;
      }
      return defaultValue;
   }

   /**
    * Converts a Serializable object of types Long/Integer/String to long value
    */
   public static long serializableToLong( Serializable serializable, long defaultValue )
   {
      try
      {
         if ( serializable != null )
         {
            if ( serializable instanceof Long )
            {
               return (Long) serializable;
            }

            if ( serializable instanceof Integer )
            {
               return Long.valueOf( (Integer) serializable );
            }

            if ( serializable instanceof String )
            {
               return Long.parseLong( (String) serializable );
            }
         }
      }
      catch ( Throwable t )
      {
         return defaultValue;
      }

      return defaultValue;
   }

   /**
    * Returns a string incremented by one.
    * Only alphanumeric digits are considered for incrementing and carry forward.
    * Digits are assumed to have most significant digit from left to the least significant digit to right (natural ordering).
    * e.g. "aZ9_z$z" is incremented to "bA0_a$a"
    */
   @SuppressWarnings( {"ConstantConditions", "UnusedAssignment"} )
   public static String incrementStringAlphaNumerically( String currentString )
   {
      char[] charArray = currentString.toCharArray();

      int carry = 1;
      for ( int j = charArray.length - 1; j >= 0; j-- )
      {
         if ( carry > 0 )
         {
            if ( ( charArray[j] >= 'a' ) && ( charArray[j] <= 'z' ) )
            {
               if ( charArray[j] != 'z' )
               {
                  charArray[j]++;
                  carry = 0;
                  break;
               }
               else
               {
                  charArray[j] = 'a';
                  carry        = 1;
               }
            }
            else if ( ( charArray[j] >= 'A' ) && ( charArray[j] <= 'Z' ) )
            {
               if ( charArray[j] != 'Z' )
               {
                  charArray[j]++;
                  carry = 0;
                  break;
               }
               else
               {
                  charArray[j] = 'A';
                  carry        = 1;
               }
            }
            else if ( ( charArray[j] >= '0' ) && ( charArray[j] <= '9' ) )
            {
               if ( charArray[j] != '9' )
               {
                  charArray[j]++;
                  carry = 0;
                  break;
               }
               else
               {
                  charArray[j] = '0';
                  carry        = 1;
               }
            }
         }
      }
      return String.valueOf( charArray );
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

      return null;
   }

   public static String pickString( Predicate<String> condition, String... strings )
   {
      return pick( condition, strings );
   }

   public static String pickNonNull( String... strings )
   {
      return pick( Objects::nonNull, strings );
   }

   public Object[] getMergedNonNullValues( Object... values )
   {
      List<Object> returnValue = Arrays.asList( values );
      returnValue.removeIf( Objects::isNull );
      return returnValue.toArray();
   }

   public static String constructURL( String... urlPieces )
   {
      String fullURL = Constants.EMPTY_STRING;

      if ( urlPieces == null )
      {
         return null;
      }

      for ( String urlPiece : urlPieces )
      {
         if ( StringUtils.isNotBlank( urlPiece ) )
         {
            fullURL = StringUtils.strip( fullURL + Constants.SLASH + StringUtils.strip( urlPiece, Constants.SLASH ), Constants.SLASH );
         }
      }

      return fullURL;
   }

   public static String getContainedKey( String source, Set<String> keySet )
   {
      for ( String key : keySet )
      {
         if ( source.contains( key ) )
         {
            return key;
         }
      }
      return null;
   }
}
