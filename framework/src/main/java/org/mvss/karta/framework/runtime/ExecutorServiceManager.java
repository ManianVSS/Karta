package org.mvss.karta.framework.runtime;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.threading.BlockingRunnableQueue;

import lombok.NoArgsConstructor;

/**
 * Class to store and manage ExecutorService for various thread groups.
 * 
 * @author Manian
 */
@NoArgsConstructor
public class ExecutorServiceManager implements AutoCloseable
{
   /**
    * The thread group name to ExecutorService mapping
    */
   private HashMap<String, ExecutorService> executorServicesMap = new HashMap<String, ExecutorService>();

   /**
    * Synchronization object
    */
   private Object                           executorSyncObject  = new Object();

   /**
    * Get the ExecutorService for the thread group by group name.
    * 
    * @param group
    * @return
    */
   public ExecutorService getExecutorServiceForGroup( String group )
   {
      return getOrAddExecutorServiceForGroup( group, 1 );
   }

   /**
    * Get the ExecutorService for the thread group by name or add a new one with the thread count
    * 
    * @param group
    * @param threadCount
    * @return
    */
   public ExecutorService getOrAddExecutorServiceForGroup( String group, int threadCount )
   {
      if ( StringUtils.isEmpty( group ) )
      {
         group = Constants.__DEFAULT__;
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

   /**
    * Add thread groups from the HashMap with the mapped thread counts.
    * 
    * @param threadGroupMap
    */
   public void addExecutorServiceForGroups( HashMap<String, Integer> threadGroupMap )
   {
      for ( Entry<String, Integer> entry : threadGroupMap.entrySet() )
      {
         getOrAddExecutorServiceForGroup( entry.getKey(), entry.getValue() );
      }
   }

   /**
    * AutoCloseable implementation to shutdown all executors before closing
    */
   @Override
   public void close()
   {
      synchronized ( executorSyncObject )
      {
         // Trigger shutdown for all executor services first.
         for ( ExecutorService executorService : executorServicesMap.values() )
         {
            if ( !executorService.isShutdown() )
            {
               executorService.shutdown();
            }
         }

         // Wait for all thread groups to shutdown.
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
}
