package org.mvss.karta.framework.utils;

import java.util.function.LongConsumer;

import org.mvss.karta.framework.core.CanThrowBooleanSupplier;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class WaitUtil
{
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
            waitIterationTask.accept( i );
         }
         catch ( Throwable t )
         {
            return WaitResult.builder().successful( false ).startTime( initialTimeStamp ).endTime( System.currentTimeMillis() ).thrown( t ).build();
         }
      }

      return WaitResult.builder().successful( false ).startTime( initialTimeStamp ).endTime( System.currentTimeMillis() ).build();
   }

   public static LongConsumer defaultWaitIterationTask = new LongConsumer()
   {
      @Override
      public void accept( long iterationNumber )
      {
         log.info( ( iterationNumber % 5 == 0 ) ? iterationNumber : "." );
      }
   };
}
