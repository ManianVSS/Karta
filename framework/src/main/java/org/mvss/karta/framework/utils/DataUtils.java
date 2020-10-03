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
}
