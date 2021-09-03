package org.mvss.karta.framework.core;

import java.util.Date;

/**
 * Utility class for step results
 *
 * @author Manian
 */
public class StandardStepResults
{
   /**
    * StepResult constant for passed step
    */
   public static StepResult passed()
   {
      return StepResult.builder().successful( true ).build();
   }

   /**
    * StepResult constant for failed step
    */
   public static StepResult failed()
   {
      return StepResult.builder().successful( false ).build();
   }

   /**
    * Create a step result with error using error message
    *
    * @param message Message to print
    * @return StepResult
    */
   public static StepResult error( String message )
   {
      return error( TestIncident.builder().message( message ).build() );
   }

   /**
    * Create a step result with error using Throwable
    *
    * @param t Throwable
    * @return StepResult
    */
   public static StepResult error( Throwable t )
   {
      return error( TestIncident.builder().message( t.getMessage() ).thrownCause( t ).build() );
   }

   /**
    * Create a step result with error using error message and Throwable
    *
    * @param message Message to print
    * @param t       Throwable
    * @return StepResult
    */
   public static StepResult error( String message, Throwable t )
   {
      return error( TestIncident.builder().message( message ).thrownCause( t ).build() );
   }

   /**
    * Create a step result with error using a test incident
    *
    * @param incident TestIncident
    * @return StepResult
    */
   public static StepResult error( TestIncident incident )
   {
      StepResult result = StepResult.builder().error( true ).build();
      result.setEndTime( new Date() );
      result.getIncidents().add( incident );
      return result;
   }

   /**
    * Create a step result with failure using Throwable
    *
    * @param t Throwable
    * @return Throwable
    */
   public static StepResult failure( Throwable t )
   {
      return failure( TestIncident.builder().thrownCause( t ).build() );
   }

   /**
    * Create a step result with failure using a test incident
    *
    * @param incident TestIncident
    * @return StepResult
    */
   public static StepResult failure( TestIncident incident )
   {
      StepResult result = StepResult.builder().successful( false ).build();
      result.setEndTime( new Date() );
      result.getIncidents().add( incident );
      return result;
   }
}
