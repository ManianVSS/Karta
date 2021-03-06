package org.mvss.karta.framework.core;

import java.util.Date;

/**
 * Utility class for feature results
 * 
 * @author Manian
 */
public class StandardFeatureResults
{
   public static FeatureResult error( String featureName, String message )
   {
      return error( featureName, TestIncident.builder().message( message ).build() );
   }

   public static FeatureResult error( String featureName, Throwable t )
   {
      return error( featureName, TestIncident.builder().message( t.getMessage() ).thrownCause( t ).build() );
   }

   public static FeatureResult error( String featureName, TestIncident incident )
   {
      FeatureResult result = FeatureResult.builder().featureName( featureName ).error( true ).build();
      result.setEndTime( new Date() );
      result.getIncidents().add( incident );
      return result;
   }

   public static FeatureResult failure( String featureName, Throwable t )
   {
      return failure( featureName, TestIncident.builder().thrownCause( t ).build() );
   }

   public static FeatureResult failure( String featureName, TestIncident incident )
   {
      FeatureResult result = FeatureResult.builder().featureName( featureName ).successful( false ).build();
      result.setEndTime( new Date() );
      result.getIncidents().add( incident );
      return result;
   }
}
