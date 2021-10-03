package org.mvss.karta.framework.utils;

import org.mvss.karta.framework.core.CanThrowBooleanSupplier;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Logger;

import java.time.Instant;
import java.util.concurrent.*;
import java.util.function.LongConsumer;

/**
 * Utility class to wait for events and on generic wait conditions specified using a functional interface
 *
 * @author Manian
 */
@Log4j2
public abstract class WaitUtil
{
   public static final  String WAIT_ITERATION                 = "Wait iteration:";
   private static final int    PROGRESS_STAGE_ITERATION_COUNT = 10;

   /**
    * Sleeps for the specified time amount without raising any interrupted exceptions
    *
    * @param milliseconds The sleep time in milliseconds.
    */
   public static void sleep( long milliseconds )
   {
      long currentTime = System.currentTimeMillis();
      long sleepUntil  = currentTime + milliseconds;

      try
      {
         Thread.sleep( sleepUntil - currentTime );
      }
      catch ( InterruptedException ignored )
      {
         if ( !( sleepUntil >= ( currentTime = System.currentTimeMillis() ) ) )
         {
            sleep( sleepUntil - currentTime );
         }
      }
   }

   /**
    * Evaluate a condition in a timeout asynchronously
    *
    * @param waitCondition CanThrowBooleanSupplier
    * @param timeOut       long
    * @return boolean
    * @throws Exception If condition evaluation throws an error
    */
   public static boolean runConditionInThreadWithTimeout( CanThrowBooleanSupplier waitCondition, long timeOut, long pollInterval ) throws Throwable
   {
      Callable<Boolean> waitConditionCallable = () -> {
         try
         {
            return waitCondition.evaluate();
         }
         catch ( Throwable e )
         {
            throw new Exception( e );
         }
      };

      ExecutorService executorService         = Executors.newSingleThreadExecutor();
      Future<Boolean> waitConditionFutureTask = executorService.submit( waitConditionCallable );

      Instant initialTimeStamp = Instant.now();
      Instant currentTimeStamp = initialTimeStamp;

      boolean indefiniteWait = ( timeOut <= 0 );

      for ( long i = 0; indefiniteWait || ( ( currentTimeStamp.toEpochMilli() - initialTimeStamp.toEpochMilli() ) < timeOut ); sleep(
               pollInterval ), currentTimeStamp = Instant.now(), i++ )
      {
         if ( waitConditionFutureTask.isDone() )
         {
            executorService.shutdown();
            if ( !executorService.awaitTermination( pollInterval, TimeUnit.MILLISECONDS ) )
            {
               log.info( "Failed to wait for executor to complete after termination." );
               executorService.shutdownNow();
            }

            return waitConditionFutureTask.get();
         }
      }

      boolean result = waitConditionFutureTask.isDone() && waitConditionFutureTask.get();

      if ( !result )
      {
         log.info( "Timed out waiting for condition evaluation to complete." );
      }

      executorService.shutdown();
      if ( !executorService.awaitTermination( pollInterval, TimeUnit.MILLISECONDS ) )
      {
         log.info( "Timed out waiting for condition evaluation. Will force shutdown of condition evaluation." );
         waitConditionFutureTask.cancel( true );
         executorService.shutdownNow();
      }
      return result;
   }

   /**
    * Wait for a generic condition to meet whose completion is evaluated using the CanThrowBooleanSupplier functional interface.
    * The condition is evaluated every poll interval specified until the time-out.
    * Upon timeout wait result is marked as failed
    * An exception also causes the wait to fail with the Throwable returned in the wait result.
    * waitIterationTask is the task to be performed on ever poll interval.
    *
    * @param waitCondition     CanThrowBooleanSupplier
    * @param timeOut           long - maximum wait time in milliseconds
    * @param pollInterval      long - poll interval in milliseconds
    * @param waitIterationTask LongConsumer - task to perform per iteration like progress.
    * @return WaitResult
    */
   public static WaitResult waitUntil( CanThrowBooleanSupplier waitCondition, long timeOut, long pollInterval, LongConsumer waitIterationTask )
   {
      Instant initialTimeStamp = Instant.now();
      Instant currentTimeStamp = initialTimeStamp;

      boolean indefiniteWait = ( timeOut <= 0 );

      for ( long i = 0; indefiniteWait || ( ( currentTimeStamp.toEpochMilli() - initialTimeStamp.toEpochMilli() ) < timeOut ); sleep(
               pollInterval ), currentTimeStamp = Instant.now(), i++ )
      {
         try
         {
            if ( runConditionInThreadWithTimeout( waitCondition, timeOut, pollInterval ) )
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
    * @param failWaitCondition CanThrowBooleanSupplier
    * @param timeOut           long - maximum wait time in milliseconds
    * @param pollInterval      long - poll interval in milliseconds
    * @param waitIterationTask LongConsumer - task to perform per iteration like progress.
    * @return WaitResult
    */
   public static WaitResult waitUntilConditionFails( CanThrowBooleanSupplier failWaitCondition, long timeOut, long pollInterval,
                                                     LongConsumer waitIterationTask )
   {
      Instant initialTimeStamp = Instant.now();
      Instant currentTimeStap  = initialTimeStamp;

      boolean indefiniteWait = ( timeOut <= 0 );

      for ( long i = 0; indefiniteWait || ( ( currentTimeStap.toEpochMilli() - initialTimeStamp.toEpochMilli() ) < timeOut ); sleep(
               pollInterval ), currentTimeStap = Instant.now(), i++ )
      {
         try
         {
            if ( !runConditionInThreadWithTimeout( failWaitCondition, timeOut, pollInterval ) )
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
   public static LongConsumer defaultWaitIterationTask = iterationNumber -> displayProgress( log, iterationNumber );

   /**
    * Displays progress to a logger
    *
    * @param log      Logger
    * @param progress long
    */
   public static void displayProgress( Logger log, long progress )
   {
      if ( progress % PROGRESS_STAGE_ITERATION_COUNT == 0 )
      {
         log.info( WAIT_ITERATION + progress );
      }
   }
}
