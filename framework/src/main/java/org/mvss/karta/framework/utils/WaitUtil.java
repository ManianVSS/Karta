package org.mvss.karta.framework.utils;

import java.time.Instant;
import java.util.function.LongConsumer;

import org.apache.logging.log4j.Logger;

import org.mvss.karta.framework.core.CanThrowBooleanSupplier;

import lombok.extern.log4j.Log4j2;

/**
 * Utility class to wait for events and on generic wait conditions specified using a functional interface
 * 
 * @author Manian
 */
@Log4j2
public abstract class WaitUtil
{
   public static final String WAIT_ITERATION                 = "Wait iteration:";
   private static final int   PROGRESS_STAGE_ITERATION_COUNT = 10;

   /**
    * Sleeps for the specified time amount without raising any interrupted exceptions
    * 
    * @param milliseconds
    */
   public static void sleep( long milliseconds )
   {
      long currentTime = System.currentTimeMillis();
      long sleepUntil = currentTime + milliseconds;

      do
      {
         try
         {
            Thread.sleep( sleepUntil - currentTime );
         }
         catch ( InterruptedException ie )
         {

         }
      }
      while ( sleepUntil > ( currentTime = System.currentTimeMillis() ) );
   }

   /**
    * Wait for a generic condition to meet whose completion is evaluated using the CanThrowBooleanSupplier functional interface.
    * The condition is evaluated every poll interval specified until the time-out.
    * Upon timeout wait result is marked as failed
    * An exception also causes the wait to fail with the Throwable returned in the wait result.
    * waitIterationTask is the task to be performed on ever poll interval.
    * 
    * @param waitCondition
    * @param timeOut
    * @param pollInterval
    * @param waitIterationTask
    * @return
    */
   public static WaitResult waitUntil( CanThrowBooleanSupplier waitCondition, long timeOut, long pollInterval, LongConsumer waitIterationTask )
   {
      Instant initialTimeStamp = Instant.now();
      Instant currentTimeStap = initialTimeStamp;

      boolean indefiniteWait = ( timeOut <= 0 );

      for ( long i = 0; indefiniteWait || ( ( currentTimeStap.toEpochMilli() - initialTimeStamp.toEpochMilli() ) < timeOut );
               sleep( pollInterval ), currentTimeStap = Instant.now(), i++ )
      {
         try
         {
            if ( waitCondition.evaluate() )
            {
               return WaitResult.builder().successful( true ).startTime( initialTimeStamp ).endTime( Instant.now() ).build();
            }
            if ( waitIterationTask != null )
            {
               waitIterationTask.accept( i );
            }
         }
         catch ( Throwable t )
         {
            return WaitResult.builder().successful( false ).startTime( initialTimeStamp ).endTime( Instant.now() ).thrown( t ).build();
         }
      }

      return WaitResult.builder().successful( false ).startTime( initialTimeStamp ).endTime( Instant.now() ).build();
   }

   /**
    * Wait for a generic condition to start failing whose completion is evaluated using the CanThrowBooleanSupplier functional interface.
    * The condition is evaluated every poll interval specified until the time-out.
    * Upon timeout wait result is marked as failed
    * An exception also causes the wait to fail with the Throwable returned in the wait result.
    * waitIterationTask is the task to be performed on ever poll interval.
    * 
    * @param failWaitCondition
    * @param timeOut
    * @param pollInterval
    * @param waitIterationTask
    * @return
    */
   public static WaitResult waitUntilConditionFails( CanThrowBooleanSupplier failWaitCondition, long timeOut, long pollInterval,
                                                     LongConsumer waitIterationTask )
   {
      Instant initialTimeStamp = Instant.now();
      Instant currentTimeStap = initialTimeStamp;

      boolean indefiniteWait = ( timeOut <= 0 );

      for ( long i = 0; indefiniteWait || ( ( currentTimeStap.toEpochMilli() - initialTimeStamp.toEpochMilli() ) < timeOut );
               sleep( pollInterval ), currentTimeStap = Instant.now(), i++ )
      {
         try
         {
            if ( !failWaitCondition.evaluate() )
            {
               return WaitResult.builder().successful( true ).startTime( initialTimeStamp ).endTime( Instant.now() ).build();
            }
            if ( waitIterationTask != null )
            {
               waitIterationTask.accept( i );
            }
         }
         catch ( Throwable t )
         {
            return WaitResult.builder().successful( false ).startTime( initialTimeStamp ).endTime( Instant.now() ).thrown( t ).build();
         }
      }

      return WaitResult.builder().successful( false ).startTime( initialTimeStamp ).endTime( Instant.now() ).build();
   }

   /**
    * The default task for waitUntil which prints the process to console
    */
   public static LongConsumer defaultWaitIterationTask = new LongConsumer()
   {
      @Override
      public void accept( long iterationNumber )
      {
         displayProgress( log, iterationNumber );
      }
   };

   /**
    * Displays progress to a logger
    * 
    * @param log
    * @param progress
    */
   public static void displayProgress( Logger log, long progress )
   {
      if ( progress % PROGRESS_STAGE_ITERATION_COUNT == 0 )
      {
         log.info( WAIT_ITERATION + progress );
      }
   }
}
