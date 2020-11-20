package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.minions.KartaMinion;
import org.mvss.karta.framework.minions.KartaMinionRegistry;
import org.mvss.karta.framework.randomization.RandomizationUtils;
import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.FeatureCompleteEvent;
import org.mvss.karta.framework.runtime.event.FeatureSetupStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.FeatureSetupStepStartEvent;
import org.mvss.karta.framework.runtime.event.FeatureStartEvent;
import org.mvss.karta.framework.runtime.event.FeatureTearDownStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.FeatureTearDownStepStartEvent;
import org.mvss.karta.framework.runtime.event.TestIncidentOccurrenceEvent;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;
import org.mvss.karta.framework.threading.BlockingRunnableQueue;
import org.mvss.karta.framework.utils.DataUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
@JsonInclude( value = Include.NON_ABSENT, content = Include.NON_ABSENT )
@Builder
public class FeatureRunner implements Callable<FeatureResult>
{
   private KartaRuntime              kartaRuntime;
   private StepRunner                stepRunner;
   private ArrayList<TestDataSource> testDataSources;
   private String                    runName;
   private TestFeature               testFeature;

   @Builder.Default
   private long                      numberOfIterations            = 1;
   @Builder.Default
   private int                       numberOfIterationsInParallel  = 1;

   @Builder.Default
   private Boolean                   chanceBasedScenarioExecution  = false;

   @Builder.Default
   private Boolean                   exclusiveScenarioPerIteration = false;

   private Consumer<FeatureResult>   resultConsumer;

   private FeatureResult             result;

   private HashSet<String>           tags;

   private synchronized void accumulateIterationResult( HashMap<String, ScenarioResult> iterationResult )
   {
      result.addIterationResult( iterationResult );
   }

   @Builder.Default
   private ArrayList<Long>            runningJobs   = new ArrayList<Long>();

   @Builder.Default
   private HashMap<Long, KartaMinion> remoteJobsMap = new HashMap<Long, KartaMinion>();

   private boolean deleteJobs()
   {
      boolean deleteJobResults = QuartzJobScheduler.deleteJobs( runningJobs );

      if ( !remoteJobsMap.isEmpty() )
      {
         for ( Entry<Long, KartaMinion> entry : remoteJobsMap.entrySet() )
         {
            try
            {
               deleteJobResults = deleteJobResults && entry.getValue().deleteJob( entry.getKey() );
            }
            catch ( Throwable t )
            {
               deleteJobResults = false;
            }
         }
      }

      if ( !deleteJobResults )
      {
         log.error( "Failed to delete test jobs" );
         result.setSuccessful( false );
      }

      return deleteJobResults;
   }

   @Override
   public FeatureResult call()
   {
      try
      {
         result = new FeatureResult();

         EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
         KartaMinionRegistry nodeRegistry = kartaRuntime.getNodeRegistry();

         Random random = kartaRuntime.getRandom();

         HashSet<String> testDataSourcePluginNames = new HashSet<String>();
         testDataSources.forEach( ( testDataSource ) -> testDataSourcePluginNames.add( testDataSource.getPluginName() ) );

         boolean useMinions = kartaRuntime.getKartaConfiguration().isMinionsEnabled() && !nodeRegistry.getMinions().isEmpty();

         if ( tags != null )
         {
            eventProcessor.featureStart( runName, testFeature, tags );
         }

         eventProcessor.raiseEvent( new FeatureStartEvent( runName, testFeature ) );

         HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
         HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

         for ( TestJob job : testFeature.getTestJobs() )
         {
            try
            {
               if ( StringUtils.isNotEmpty( job.getNode() ) )
               {
                  KartaMinion node = nodeRegistry.getNode( job.getNode() );
                  long jobId = node.scheduleJob( stepRunner.getPluginName(), testDataSourcePluginNames, runName, testFeature.getName(), job );

                  if ( jobId != -1 )
                  {
                     remoteJobsMap.put( jobId, node );
                  }
               }
               else
               {
                  long jobId = kartaRuntime.startScheduledJob( stepRunner, testDataSources, runName, testFeature.getName(), job );

                  if ( jobId != -1 )
                  {
                     runningJobs.add( jobId );
                  }
               }
            }
            catch ( Throwable t )
            {
               log.error( "Exception occured while scheduling jobs ", t );
               if ( !deleteJobs() )
               {
                  log.error( "Failed to delete test jobs" );
                  result.setSuccessful( false );
               }
            }
         }

         long iterationIndex = -1;
         int stepIndex = 0;

         HashMap<TestScenario, AtomicLong> scenarioIterationIndexMap = new HashMap<TestScenario, AtomicLong>();
         testFeature.getTestScenarios().forEach( ( scenario ) -> scenarioIterationIndexMap.put( scenario, new AtomicLong() ) );

         stepIndex = 0;
         for ( TestStep step : testFeature.getSetupSteps() )
         {
            TestExecutionContext testExecutionContext = new TestExecutionContext( runName, testFeature.getName(), iterationIndex, Constants.__FEATURE_SETUP__, step.getIdentifier(), testData, variables );

            testData = KartaRuntime.getMergedTestData( runName, step.getTestData(), step.getTestDataSet(), testDataSources, new ExecutionStepPointer( testFeature.getName(), Constants.__FEATURE_SETUP__,
                                                                                                                                                      stepRunner.sanitizeStepDefinition( step.getIdentifier() ), iterationIndex, stepIndex++ ) );
            // log.debug( "Step test data is " + testData.toString() );
            testExecutionContext.setData( testData );

            StepResult stepResult = new StepResult();
            stepResult.setSuccessful( true );

            eventProcessor.raiseEvent( new FeatureSetupStepStartEvent( runName, testFeature, step ) );

            try
            {
               if ( StringUtils.isNotEmpty( step.getNode() ) )
               {
                  // TODO: Handle null node error
                  stepResult = nodeRegistry.getNode( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
               }
               else
               {
                  stepResult = stepRunner.runStep( step, testExecutionContext );
               }

               for ( TestIncident incident : stepResult.getIncidents() )
               {
                  eventProcessor.raiseEvent( new TestIncidentOccurrenceEvent( runName, testFeature.getName(), iterationIndex, Constants.__FEATURE_SETUP__, step.getIdentifier(), incident ) );
               }

               for ( Event event : stepResult.getEvents() )
               {
                  eventProcessor.raiseEvent( event );
               }

               DataUtils.mergeVariables( stepResult.getResults(), variables );
            }
            catch ( TestFailureException tfe )
            {
               log.error( "Exception in test failure ", tfe );
               stepResult.setSuccessful( false );
               TestIncident incident = TestIncident.builder().thrownCause( tfe ).build();
               stepResult.addIncident( incident );
            }
            finally
            {
               eventProcessor.raiseEvent( new FeatureSetupStepCompleteEvent( runName, testFeature, step, stepResult ) );

               result.getSetupResultMap().put( step.getIdentifier(), stepResult.isPassed() );
               result.getIncidents().addAll( stepResult.getIncidents() );

               if ( !stepResult.isPassed() )
               {
                  result.setSuccessful( false );

                  if ( !deleteJobs() )
                  {
                     log.error( "Failed to delete test jobs" );
                     result.setSuccessful( false );
                  }

                  eventProcessor.raiseEvent( new FeatureCompleteEvent( runName, testFeature, result ) );

                  if ( tags != null )
                  {
                     eventProcessor.featureStop( runName, testFeature, tags );
                  }

                  return result;
               }
            }
         }

         if ( !DataUtils.inRange( numberOfIterations, 1, Integer.MAX_VALUE ) )
         {
            log.error( "Configuration error: invalid number of iterations: " + numberOfIterations );
            numberOfIterations = 1;
         }

         if ( !DataUtils.inRange( numberOfIterationsInParallel, 1, Integer.MAX_VALUE ) )
         {
            log.error( "Configuration error: invalid number of threads: " + numberOfIterationsInParallel );
            numberOfIterationsInParallel = 1;
         }

         ExecutorService iterationExecutionService = new ThreadPoolExecutor( numberOfIterationsInParallel, numberOfIterationsInParallel, 0L, TimeUnit.MILLISECONDS, new BlockingRunnableQueue( numberOfIterationsInParallel ) );

         for ( iterationIndex = 0; ( numberOfIterations <= 0 ) || ( iterationIndex < numberOfIterations ); iterationIndex++ )
         {
            ArrayList<TestScenario> scenariosToRun = new ArrayList<TestScenario>();

            if ( chanceBasedScenarioExecution )
            {
               if ( exclusiveScenarioPerIteration )
               {
                  TestScenario scenarioToRun = RandomizationUtils.generateNextMutexComposition( random, testFeature.getTestScenarios() );

                  if ( scenarioToRun != null )
                  {
                     scenariosToRun.add( scenarioToRun );
                  }
                  else
                  {
                     continue;
                  }
               }
               else
               {
                  scenariosToRun.addAll( RandomizationUtils.generateNextComposition( random, testFeature.getTestScenarios() ) );
               }
            }
            else
            {
               scenariosToRun = testFeature.getTestScenarios();
            }

            IterationRunner iterationRunner = IterationRunner.builder().kartaRuntime( kartaRuntime ).stepRunner( stepRunner ).testDataSources( testDataSources ).runName( runName ).featureName( testFeature.getName() )
                     .scenarioSetupSteps( testFeature.getScenarioSetupSteps() ).scenariosToRun( scenariosToRun ).scenarioTearDownSteps( testFeature.getScenarioTearDownSteps() ).iterationIndex( iterationIndex )
                     .scenarioIterationIndexMap( scenarioIterationIndexMap ).variables( DataUtils.cloneMap( variables ) ).resultConsumer( ( iterationResult ) -> accumulateIterationResult( iterationResult ) ).tags( tags ).build();

            if ( useMinions )
            {
               KartaMinion minion = nodeRegistry.getNextMinion();

               if ( minion != null )
               {
                  iterationRunner.setMinionToUse( minion );
               }
            }

            if ( numberOfIterationsInParallel == 1 )
            {
               log.debug( "Iteration start " + iterationIndex + " with scenarios " + scenariosToRun );
               iterationRunner.call();
            }
            else
            {
               log.debug( "Iteration queued " + iterationIndex + " with scenarios " + scenariosToRun );
               iterationExecutionService.submit( iterationRunner );
            }
         }

         iterationExecutionService.shutdown();
         iterationExecutionService.awaitTermination( Long.MAX_VALUE, TimeUnit.NANOSECONDS );

         testFeature.getTestScenarios().forEach( ( scenario ) -> scenarioIterationIndexMap.get( scenario ).set( 0 ) );

         iterationIndex = -1;
         stepIndex = 0;
         for ( TestStep step : testFeature.getTearDownSteps() )
         {
            TestExecutionContext testExecutionContext = new TestExecutionContext( runName, testFeature.getName(), iterationIndex, Constants.__FEATURE_TEARDOWN__, step.getIdentifier(), testData, variables );

            testData = KartaRuntime.getMergedTestData( runName, step.getTestData(), step.getTestDataSet(), testDataSources, new ExecutionStepPointer( testFeature.getName(), Constants.__FEATURE_TEARDOWN__,
                                                                                                                                                      stepRunner.sanitizeStepDefinition( step.getIdentifier() ), iterationIndex, stepIndex++ ) );
            // log.debug( "Step test data is " + testData.toString() );
            testExecutionContext.setData( testData );

            StepResult stepResult = new StepResult();
            eventProcessor.raiseEvent( new FeatureTearDownStepStartEvent( runName, testFeature, step ) );

            try
            {
               if ( StringUtils.isNotEmpty( step.getNode() ) )
               {
                  stepResult = nodeRegistry.getNode( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
               }
               else
               {
                  stepResult = stepRunner.runStep( step, testExecutionContext );
               }

               for ( TestIncident incident : stepResult.getIncidents() )
               {
                  eventProcessor.raiseEvent( new TestIncidentOccurrenceEvent( runName, testFeature.getName(), iterationIndex, Constants.__FEATURE_SETUP__, step.getIdentifier(), incident ) );
               }

               for ( Event event : stepResult.getEvents() )
               {
                  eventProcessor.raiseEvent( event );
               }

               DataUtils.mergeVariables( stepResult.getResults(), variables );
            }
            catch ( TestFailureException tfe )
            {
               log.error( "Exception in test failure ", tfe );
               stepResult.setSuccessful( false );
               TestIncident incident = TestIncident.builder().thrownCause( tfe ).build();
               stepResult.addIncident( incident );
            }
            finally
            {
               eventProcessor.raiseEvent( new FeatureTearDownStepCompleteEvent( runName, testFeature, step, stepResult ) );

               result.getTearDownResultMap().put( step.getIdentifier(), stepResult.isPassed() );
               result.getIncidents().addAll( stepResult.getIncidents() );

               if ( !stepResult.isPassed() )
               {
                  result.setSuccessful( false );
                  continue;
               }
            }
         }

         if ( !deleteJobs() )
         {
            log.error( "Failed to delete test jobs" );
            result.setSuccessful( false );
         }

         eventProcessor.raiseEvent( new FeatureCompleteEvent( runName, testFeature, result ) );
         if ( tags != null )
         {
            eventProcessor.featureStop( runName, testFeature, tags );
         }
      }
      catch ( Throwable t )
      {
         log.error( "Exception occured during feature run", t );
         log.error( ExceptionUtils.getStackTrace( t ) );
         result.setError( true );
      }

      if ( resultConsumer != null )
      {
         resultConsumer.accept( result );
      }
      return result;
   }
}
