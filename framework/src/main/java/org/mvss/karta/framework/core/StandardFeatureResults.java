package org.mvss.karta.framework.core;

import java.util.Date;

/**
 * Utility class for feature results
 * 
 * @author Manian
 */
public class StandardFeatureResults
{
   public final static FeatureResult passed = FeatureResult.builder().successful( true ).build();
   public final static FeatureResult failed = FeatureResult.builder().successful( false ).build();

   public static FeatureResult error( String message )
   {
      return error( TestIncident.builder().message( message ).build() );
   }

   public static FeatureResult error( Throwable t )
   {
      return error( TestIncident.builder().message( t.getMessage() ).thrownCause( t ).build() );
   }

   public static FeatureResult error( TestIncident incident )
   {
      FeatureResult result = FeatureResult.builder().error( true ).build();
      result.setEndTime( new Date() );
      result.getIncidents().add( incident );
      return result;
   }

   public static FeatureResult failure( Throwable t )
   {
      return failure( TestIncident.builder().thrownCause( t ).build() );
   }

   public static FeatureResult failure( TestIncident incident )
   {
      FeatureResult result = FeatureResult.builder().successful( false ).build();
      result.setEndTime( new Date() );
      result.getIncidents().add( incident );
      return result;
   }
}
