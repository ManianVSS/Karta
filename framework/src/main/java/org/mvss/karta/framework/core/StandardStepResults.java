package org.mvss.karta.framework.core;

import java.util.Date;

public class StandardStepResults
{
   public final static StepResult passed = StepResult.builder().successful( true ).build();
   public final static StepResult failed = StepResult.builder().successful( false ).build();

   public static StepResult error( String message )
   {
      return error( TestIncident.builder().message( message ).build() );
   }

   public static StepResult error( Throwable t )
   {
      return error( TestIncident.builder().message( t.getMessage() ).thrownCause( t ).build() );
   }

   public static StepResult error( String message, Throwable t )
   {
      return error( TestIncident.builder().message( message ).thrownCause( t ).build() );
   }

   public static StepResult error( TestIncident incident )
   {
      StepResult result = StepResult.builder().error( true ).build();
      result.setEndTime( new Date() );
      result.getIncidents().add( incident );
      return result;
   }

   public static StepResult failure( Throwable t )
   {
      return failure( TestIncident.builder().thrownCause( t ).build() );
   }

   public static StepResult failure( TestIncident incident )
   {
      StepResult result = StepResult.builder().successful( false ).build();
      result.setEndTime( new Date() );
      result.getIncidents().add( incident );
      return result;
   }
}
