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

   public static boolean inRange( byte value, byte min, byte max )
   {
      return ( value >= min ) && ( value <= max );
   }

   public static boolean inRange( int value, int min, int max )
   {
      return ( value >= min ) && ( value <= max );
   }

   public static boolean inRange( long value, long min, long max )
   {
      return ( value >= min ) && ( value <= max );
   }

   public static boolean inRange( float value, float min, float max )
   {
      return ( value >= min ) && ( value <= max );
   }

   public static boolean inRange( double value, double min, double max )
   {
      return ( value >= min ) && ( value <= max );
   }

}
