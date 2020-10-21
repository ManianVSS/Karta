package org.mvss.karta.framework.runtime.event;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.runtime.TestFailureException;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.TestEventListener;
import org.mvss.karta.framework.threading.BlockingRunnableQueue;
import org.mvss.karta.framework.utils.WaitUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EventProcessor implements AutoCloseable
{
   private static final long          POLL_TIME_FOR_EVENT_QUEUE_CLEARING = 1000;

   @Getter
   @Setter
   @PropertyMapping( "EventProcessor.numberOfThread" )
   private int                        numberOfThread                     = 1;

   @Getter
   @Setter
   @PropertyMapping( "EventProcessor.maxEventQueueSize" )
   private int                        maxEventQueueSize                  = 100;

   private ExecutorService            eventListenerExecutorService       = null;
   private HashSet<TestEventListener> testEventListeners                 = new HashSet<TestEventListener>();

   private BlockingRunnableQueue      eventProcessingQueue;

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
      eventProcessingQueue = new BlockingRunnableQueue( maxEventQueueSize );
      eventListenerExecutorService = new ThreadPoolExecutor( numberOfThread, numberOfThread, 0L, TimeUnit.MILLISECONDS, eventProcessingQueue );
   }

   @Override
   public void close()
   {
      try
      {
         while ( !eventProcessingQueue.isEmpty() )
         {
            WaitUtil.sleep( POLL_TIME_FOR_EVENT_QUEUE_CLEARING );
         }
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
            eventListenerExecutorService.submit( () -> testEventListener.processEvent( event ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void fail( String runName, TestIncident incident ) throws TestFailureException
   {
      if ( incident != null )
      {
         raiseEvent( new TestIncidentOccurenceEvent( runName, incident ) );
         throw new TestFailureException( incident.getMessage(), incident.getThrownCause() );
      }
   }

   public void raiseIncident( String runName, TestIncident incident )
   {
      if ( incident != null )
      {
         raiseEvent( new TestIncidentOccurenceEvent( runName, incident ) );
      }
   }
}
