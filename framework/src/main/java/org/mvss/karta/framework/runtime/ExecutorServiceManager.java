package org.mvss.karta.framework.runtime;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.threading.BlockingRunnableQueue;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ExecutorServiceManager implements AutoCloseable
{
   private HashMap<String, ExecutorService> executorServicesMap = new HashMap<String, ExecutorService>();
   private Object                           executorSyncObject  = new Object();

   public ExecutorService getExecutorServiceForGroup( String group )
   {
      return getOrAddExecutorServiceForGroup( group, 1 );
   }

   public ExecutorService getOrAddExecutorServiceForGroup( String group, int threadCount )
   {
      if ( StringUtils.isEmpty( group ) )
      {
         group = Constants.__TESTS__;
      }

      synchronized ( executorSyncObject )
      {
         if ( !executorServicesMap.containsKey( group ) )
         {
            executorServicesMap.put( group, new ThreadPoolExecutor( threadCount, threadCount, 0L, TimeUnit.MILLISECONDS, new BlockingRunnableQueue( threadCount ) ) );
         }
      }

      return executorServicesMap.get( group );
   }

   @Override
   public void close()
   {
      for ( ExecutorService executorService : executorServicesMap.values() )
      {
         if ( !executorService.isShutdown() )
         {
            executorService.shutdown();
         }
      }

      for ( ExecutorService executorService : executorServicesMap.values() )
      {
         try
         {
            if ( !executorService.isShutdown() )
            {
               executorService.shutdown();
            }

            executorService.awaitTermination( Long.MAX_VALUE, TimeUnit.NANOSECONDS );
         }
         catch ( InterruptedException e )
         {
            continue;
         }
      }
   }
}
