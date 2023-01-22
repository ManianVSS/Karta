package org.mvss.karta.framework.utils;

import org.mvss.karta.framework.threading.BlockingRunnableQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;

public class ThreadUtils
{
   public static void waitForAllThreads( List<Thread> threadsToWaitFor )
   {
      for ( Thread threadToWait : threadsToWaitFor )
      {
         try
         {
            threadToWait.join();
         }
         catch ( InterruptedException ignored )
         {
         }
      }
   }

   public static <T> boolean runCallableInParallel( List<Callable<T>> callables, Function<T, Boolean> resultConsumer, int threads ) throws Throwable
   {
      ExecutorService callableExecutorService = new ThreadPoolExecutor( threads, threads, 0L, TimeUnit.MILLISECONDS,
               new BlockingRunnableQueue( threads ) );
      ArrayList<Future<T>> futuresList = new ArrayList<>();
      for ( Callable<T> callable : callables )
      {
         futuresList.add( callableExecutorService.submit( callable ) );
      }
      callableExecutorService.shutdown();

      boolean waitTerminationResult = callableExecutorService.awaitTermination( Long.MAX_VALUE, TimeUnit.SECONDS );

      ArrayList<Throwable> exceptionArrayList = new ArrayList<>();
      for ( Future<T> future : futuresList )
      {
         try
         {
            resultConsumer.apply( future.get() );
         }
         catch ( ExecutionException e )
         {
            exceptionArrayList.add( e.getCause() );
         }
      }

      if ( !exceptionArrayList.isEmpty() )
      {
         throw new ParallelCausesException( exceptionArrayList );
      }

      return waitTerminationResult;
   }

   public static <T> boolean runCallablesInSerial( List<Callable<T>> callables, Function<T, Boolean> resultConsumer ) throws Throwable
   {
      for ( Callable<?> callable : callables )
      {
         //noinspection unchecked
         T result = (T) callable.call();
         if ( ( resultConsumer != null ) && !resultConsumer.apply( result ) )
         {
            return false;
         }
      }
      return true;
   }

   public static <T> boolean runCallables( List<Callable<T>> callables, Function<T, Boolean> resultConsumer, boolean isParallel, int numberOfThreads )
            throws Throwable
   {
      if ( isParallel )
      {
         return runCallableInParallel( callables, resultConsumer, numberOfThreads );
      }
      else
      {
         return runCallablesInSerial( callables, resultConsumer );
      }
   }
}
