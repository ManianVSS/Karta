package org.mvss.karta.framework.runtime.event;

import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
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

   public void raiseRunStartedEvent( String runName )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.runStarted( runName ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseFeatureStartedEvent( String runName, TestFeature feature )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.featureStarted( runName, feature ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseFeatureSetupStepStartedEvent( String runName, TestFeature feature, TestStep setupStep )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.featureSetupStepStarted( runName, feature, setupStep ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseFeatureSetupStepCompletedEvent( String runName, TestFeature feature, TestStep setupStep, StepResult result )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.featureSetupStepComplete( runName, feature, setupStep, result ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseScenarioStartedEvent( String runName, TestFeature feature, long iterationNumber, TestScenario scenario )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.scenarioStarted( runName, feature, iterationNumber, scenario ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseScenarioSetupStepStartedEvent( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioSetupStep )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.scenarioSetupStepStarted( runName, feature, iterationNumber, scenario, scenarioSetupStep ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseScenarioSetupStepCompletedEvent( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioSetupStep, StepResult result )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.scenarioSetupStepCompleted( runName, feature, iterationNumber, scenario, scenarioSetupStep, result ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseScenarioChaosActionStartedEvent( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, ChaosAction action )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.scenarioChaosActionStarted( runName, feature, iterationNumber, scenario, action ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseScenarioChaosActionCompletedEvent( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, ChaosAction action, StepResult result )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.scenarioChaosActionCompleted( runName, feature, iterationNumber, scenario, action, result ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseScenarioStepStartedEvent( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioStep )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.scenarioStepStarted( runName, feature, iterationNumber, scenario, scenarioStep ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseScenarioStepCompletedEvent( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioStep, StepResult result )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.scenarioStepCompleted( runName, feature, iterationNumber, scenario, scenarioStep, result ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseScenarioTearDownStepStartedEvent( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioTearDownStep )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.scenarioTearDownStepStarted( runName, feature, iterationNumber, scenario, scenarioTearDownStep ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseScenarioTearDownStepCompletedEvent( String runName, TestFeature feature, long iterationNumber, TestScenario scenario, TestStep scenarioTearDownStep, StepResult result )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.scenarioTearDownStepCompleted( runName, feature, iterationNumber, scenario, scenarioTearDownStep, result ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseScenarioCompletedEvent( String runName, TestFeature feature, long iterationNumber, TestScenario scenario )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.scenarioCompleted( runName, feature, iterationNumber, scenario ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseFeatureTearDownStepStartedEvent( String runName, TestFeature feature, TestStep tearDownStep )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.featureTearDownStepStarted( runName, feature, tearDownStep ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseFeatureTearDownStepCompleteEvent( String runName, TestFeature feature, TestStep tearDownStep, StepResult result )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.featureTearDownStepComplete( runName, feature, tearDownStep, result ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseFeatureCompletedEvent( String runName, TestFeature feature )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.featureCompleted( runName, feature ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }

   public void raiseRunCompletedEvent( String runName )
   {
      for ( TestEventListener testEventListener : testEventListeners )
      {
         try
         {
            eventListenerExecutorService.submit( () -> testEventListener.runCompleted( runName ) );
         }
         catch ( Throwable t )
         {
            log.error( "Exception occured during event processing ", t );
            continue;
         }
      }
   }
}
