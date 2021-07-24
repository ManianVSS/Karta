package org.mvss.karta.framework.runtime.event;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.mvss.karta.framework.core.PreparedScenario;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.runtime.TestFailureException;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.TestEventListener;
import org.mvss.karta.framework.runtime.interfaces.TestLifeCycleHook;
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

   private HashSet<TestLifeCycleHook> lifeCycleHooks                     = new HashSet<TestLifeCycleHook>();

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

   public boolean addLifeCycleHook( TestLifeCycleHook testEventListener )
   {
      return lifeCycleHooks.add( testEventListener );
   }

   public boolean removeLifeCycleHook( TestLifeCycleHook testEventListener )
   {
      return lifeCycleHooks.remove( testEventListener );
   }

   public void start()
   {
      // TODO: Change to thread factory to be able to manage threads.
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

   private void sendEventsToListeners( Event event )
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

   public void raiseEvent( Event event )
   {
      sendEventsToListeners( event );
   }

   public void fail( String runName, String featureName, Long iterationIndex, String scenarioName, String stepIdentifier, TestIncident incident )
            throws TestFailureException
   {
      if ( incident != null )
      {
         raiseEvent( new TestIncidentOccurrenceEvent( runName, featureName, iterationIndex, scenarioName, stepIdentifier, incident ) );
         throw new TestFailureException( incident.getMessage(), incident.getThrownCause() );
      }
   }

   public void raiseIncident( String runName, String featureName, Long iterationIndex, String scenarioName, String stepIdentifier,
                              TestIncident incident )
   {
      if ( incident != null )
      {
         raiseEvent( new TestIncidentOccurrenceEvent( runName, featureName, iterationIndex, scenarioName, stepIdentifier, incident ) );
      }
   }

   public boolean runStart( String runName, HashSet<String> tags )
   {
      boolean success = true;

      for ( TestLifeCycleHook lifeCycleHook : lifeCycleHooks )
      {
         success = success && lifeCycleHook.runStart( runName, tags );
      }

      return success;
   }

   public boolean featureStart( String runName, TestFeature feature, HashSet<String> tags )
   {
      boolean success = true;

      if ( tags != null )
      {
         for ( TestLifeCycleHook lifeCycleHook : lifeCycleHooks )
         {
            success = success && lifeCycleHook.featureStart( runName, feature, tags );
         }
      }

      return success;
   }

   public boolean scenarioStart( String runName, String featureName, PreparedScenario scenario, HashSet<String> tags )
   {
      boolean success = true;

      if ( tags != null )
      {
         for ( TestLifeCycleHook lifeCycleHook : lifeCycleHooks )
         {
            success = success && lifeCycleHook.scenarioStart( runName, featureName, scenario, tags );
         }
      }

      return success;
   }

   public boolean scenarioStop( String runName, String featureName, PreparedScenario scenario, HashSet<String> tags )
   {
      boolean success = true;

      if ( tags != null )
      {
         for ( TestLifeCycleHook lifeCycleHook : lifeCycleHooks )
         {
            success = success && lifeCycleHook.scenarioStop( runName, featureName, scenario, tags );
         }
      }

      return success;
   }

   public boolean scenarioFailed( String runName, String featureName, PreparedScenario scenario, HashSet<String> tags, ScenarioResult scenarioResult )
   {
      boolean success = true;

      if ( tags != null )
      {
         for ( TestLifeCycleHook lifeCycleHook : lifeCycleHooks )
         {
            success = success && lifeCycleHook.scenarioFailed( runName, featureName, scenario, tags, scenarioResult );
         }
      }

      return success;
   }

   public boolean featureStop( String runName, TestFeature feature, HashSet<String> tags )
   {
      boolean success = true;

      if ( tags != null )
      {
         for ( TestLifeCycleHook lifeCycleHook : lifeCycleHooks )
         {
            success = success && lifeCycleHook.featureStop( runName, feature, tags );
         }
      }

      return success;
   }

   public boolean runStop( String runName, HashSet<String> tags )
   {
      boolean success = true;

      for ( TestLifeCycleHook lifeCycleHook : lifeCycleHooks )
      {
         success = success && lifeCycleHook.runStop( runName, tags );
      }

      return success;
   }
}
