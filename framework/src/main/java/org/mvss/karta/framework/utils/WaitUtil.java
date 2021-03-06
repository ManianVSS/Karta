package org.mvss.karta.framework.utils;

import java.util.function.LongConsumer;

import org.mvss.karta.framework.core.CanThrowBooleanSupplier;

/**
 * Utility class to wait for events and on generic wait conditions specified using a functional interface
 * 
 * @author Manian
 */
public class WaitUtil
{
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
    * Wait for a generic event whose completion is evaluated using the CanThrowBooleanSupplier functional interface.
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
      long initialTimeStamp = System.currentTimeMillis();
      long currentTimeStap = initialTimeStamp;

      boolean indefiniteWait = ( timeOut <= 0 );

      for ( long i = 0; indefiniteWait || ( ( currentTimeStap - initialTimeStamp ) < timeOut ); sleep( pollInterval ), currentTimeStap = System.currentTimeMillis(), i++ )
      {
         try
         {
            if ( waitCondition.evaluate() )
            {
               return WaitResult.builder().successful( true ).startTime( initialTimeStamp ).endTime( System.currentTimeMillis() ).build();
            }
            if ( waitIterationTask != null )
            {
               waitIterationTask.accept( i );
            }
         }
         catch ( Throwable t )
         {
            return WaitResult.builder().successful( false ).startTime( initialTimeStamp ).endTime( System.currentTimeMillis() ).thrown( t ).build();
         }
      }

      return WaitResult.builder().successful( false ).startTime( initialTimeStamp ).endTime( System.currentTimeMillis() ).build();
   }

   /**
    * The default task for waitUntil which prints the process to console
    */
   public static LongConsumer defaultWaitIterationTask = new LongConsumer()
   {
      @Override
      public void accept( long iterationNumber )
      {
         System.out.print( ( iterationNumber % 5 == 0 ) ? iterationNumber : "." );
      }
   };
}
