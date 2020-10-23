package org.mvss.karta.framework.utils;

import java.io.Serializable;
import java.util.HashMap;

public class DataUtils
{

   public static void mergeVariables( HashMap<String, Serializable> sourceVars, HashMap<String, Serializable> destinationVars )
   {
      if ( ( sourceVars != null ) && ( sourceVars != destinationVars ) )
      {
         for ( String variableName : sourceVars.keySet() )
         {
            destinationVars.put( variableName, sourceVars.get( variableName ) );
         }
      }
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
