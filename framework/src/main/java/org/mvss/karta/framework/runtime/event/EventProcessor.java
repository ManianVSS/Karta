package org.mvss.karta.framework.runtime.event;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.TestEventListener;
import org.mvss.karta.framework.threading.BlockingRunnableQueue;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EventProcessor
{
   @Getter
   @Setter
   @PropertyMapping( "EventProcessor.numberOfThread" )
   private int                        numberOfThread               = 1;

   @Getter
   @Setter
   @PropertyMapping( "EventProcessor.maxEventQueueSize" )
   private int                        maxEventQueueSize            = 100;

   private ExecutorService            eventListenerExecutorService = null;
   private HashSet<TestEventListener> testEventListeners           = new HashSet<TestEventListener>();

   public boolean addEventListener( TestEventListener testEventListener )
   {
      return testEventListeners.add( testEventListener );
   }

   public boolean removeEventListener( TestEventListener testEventListener )
   {
      return testEventListeners.remove( testEventListener );
   }

   public void start()
   {
      // TODO: Change to thread factory to be able to manage events.
      eventListenerExecutorService = new ThreadPoolExecutor( numberOfThread, numberOfThread, 0L, TimeUnit.MILLISECONDS, new BlockingRunnableQueue( maxEventQueueSize ) );
   }

   public void stop()
   {
      try
      {
         eventListenerExecutorService.shutdown();
         eventListenerExecutorService.awaitTermination( Long.MAX_VALUE, TimeUnit.NANOSECONDS );
      }
      catch ( Throwable t )
      {
         log.error( "Exception occured while stopping event processor ", t );
      }
   }

   public void raiseEvent( Event event )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.testEvent( event ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }
}
