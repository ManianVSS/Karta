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
   public final static StepResult passed = StepResult.builder().successful( true ).build();
   /**
    * StepResult constant for failed step
    */
   public final static StepResult failed = StepResult.builder().successful( false ).build();

   /**
    * Create a step result with error using error message
    * 
    * @param message
    * @return
    */
   public static StepResult error( String message )
   {
      return error( TestIncident.builder().message( message ).build() );
   }

   /**
    * Create a step result with error using Throwable
    * 
    * @param t
    * @return
    */
   public static StepResult error( Throwable t )
   {
      return error( TestIncident.builder().message( t.getMessage() ).thrownCause( t ).build() );
   }

   /**
    * Create a step result with error using error message and Throwable
    * 
    * @param message
    * @param t
    * @return
    */
   public static StepResult error( String message, Throwable t )
   {
      return error( TestIncident.builder().message( message ).thrownCause( t ).build() );
   }

   /**
    * Create a step result with error using a test incident
    * 
    * @param incident
    * @return
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
    * @param t
    * @return
    */
   public static StepResult failure( Throwable t )
   {
      return failure( TestIncident.builder().thrownCause( t ).build() );
   }

   /**
    * Create a step result with failure using a test incident
    * 
    * @param incident
    * @return
    */
   public static StepResult failure( TestIncident incident )
   {
      StepResult result = StepResult.builder().successful( false ).build();
      result.setEndTime( new Date() );
      result.getIncidents().add( incident );
      return result;
   }
}
