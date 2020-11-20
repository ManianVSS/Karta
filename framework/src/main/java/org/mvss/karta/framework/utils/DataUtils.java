package org.mvss.karta.framework.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class DataUtils
{
   public static <K, V> HashMap<K, V> cloneMap( HashMap<K, V> source )
   {
      HashMap<K, V> clone = new HashMap<K, V>();
      clone.putAll( source );
      return clone;
   }

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

   public static <K, V> void mergeVariables( Map<K, V> sourceVars, Map<K, V> destinationVars )
   {
      if ( ( sourceVars != null ) && ( sourceVars != destinationVars ) )
      {
         for ( K variableName : sourceVars.keySet() )
         {
            destinationVars.put( variableName, sourceVars.get( variableName ) );
         }
      }
   }

   public static <T> void addItemToTreeMapInSequence( T method, TreeMap<Integer, ArrayList<T>> map, Integer sequence )
   {
      if ( ( map == null ) || ( method == null ) )
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

      map.get( sequence ).add( method );
   }

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

   public static boolean inRange( byte value, byte min, byte max, boolean includeMin, boolean includeMax )
   {
      return ( includeMin ? ( value >= min ) : ( value > min ) ) && ( includeMax ? ( value <= max ) : ( value < max ) );
   }

   public static boolean inRange( int value, int min, int max, boolean includeMin, boolean includeMax )
   {
      return ( includeMin ? ( value >= min ) : ( value > min ) ) && ( includeMax ? ( value <= max ) : ( value < max ) );
   }

   public static boolean inRange( long value, long min, long max, boolean includeMin, boolean includeMax )
   {
      return ( includeMin ? ( value >= min ) : ( value > min ) ) && ( includeMax ? ( value <= max ) : ( value < max ) );
   }

   public static boolean inRange( float value, float min, float max, boolean includeMin, boolean includeMax )
   {
      return ( includeMin ? ( value >= min ) : ( value > min ) ) && ( includeMax ? ( value <= max ) : ( value < max ) );
   }

   public static boolean inRange( double value, double min, double max, boolean includeMin, boolean includeMax )
   {
      return ( includeMin ? ( value >= min ) : ( value > min ) ) && ( includeMax ? ( value <= max ) : ( value < max ) );
   }

   public static boolean inRange( byte value, byte min, byte max )
   {
      return inRange( value, min, max, true, true );
   }

   public static boolean inRange( int value, int min, int max )
   {
      return inRange( value, min, max, true, true );
   }

   public static boolean inRange( long value, long min, long max )
   {
      return inRange( value, min, max, true, true );
   }

   public static boolean inRange( float value, float min, float max )
   {
      return inRange( value, min, max, true, true );
   }

   public static boolean inRange( double value, double min, double max )
   {
      return inRange( value, min, max, true, true );
   }

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

}
