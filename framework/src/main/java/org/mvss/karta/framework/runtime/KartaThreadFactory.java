package org.mvss.karta.framework.runtime;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class KartaThreadFactory implements ThreadFactory
{
   private AtomicInteger                            threadCounter          = new AtomicInteger();

   private ConcurrentHashMap<KartaRunnable, Thread> kartaRunnableThreadMap = new ConcurrentHashMap<KartaRunnable, Thread>();

   private Object                                   lock                   = new Object();

   @Override
   public Thread newThread( Runnable r )
   {
      Thread thread = new Thread( r, Constants.KARTA + Constants.HYPHEN + threadCounter.incrementAndGet() );

      synchronized ( lock )
      {
         if ( r instanceof KartaRunnable )
         {
            kartaRunnableThreadMap.put( (KartaRunnable) r, thread );
         }
      }
      clearNonRunningThreads();

      return thread;
   }

   public void clearNonRunningThreads()
   {
      for ( KartaRunnable kartaRunnable : kartaRunnableThreadMap.keySet() )
      {
         if ( !kartaRunnableThreadMap.get( kartaRunnable ).isAlive() )
         {
            kartaRunnableThreadMap.remove( kartaRunnable );
         }
      }
   }

   public void shutdown()
   {
      synchronized ( lock )
      {
         for ( KartaRunnable kartaRunnable : kartaRunnableThreadMap.keySet() )
         {
            if ( kartaRunnableThreadMap.get( kartaRunnable ).isAlive() )
            {
               kartaRunnable.shutdown();
            }
         }
         kartaRunnableThreadMap.clear();
      }
   }

}
