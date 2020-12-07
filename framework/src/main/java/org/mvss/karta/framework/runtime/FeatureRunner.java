package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.SerializableKVP;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.core.TestJobIterationResultProcessor;
import org.mvss.karta.framework.core.TestJobResult;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.minions.KartaMinion;
import org.mvss.karta.framework.minions.KartaMinionRegistry;
import org.mvss.karta.framework.randomization.RandomizationUtils;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.FeatureCompleteEvent;
import org.mvss.karta.framework.runtime.event.FeatureSetupStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.FeatureSetupStepStartEvent;
import org.mvss.karta.framework.runtime.event.FeatureStartEvent;
import org.mvss.karta.framework.runtime.event.FeatureTearDownStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.FeatureTearDownStepStartEvent;
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
   private KartaRuntime            kartaRuntime;

   private RunInfo                 runInfo;

   private TestFeature             testFeature;

   private Consumer<FeatureResult> resultConsumer;

   private FeatureResult           result;

   private synchronized void accumulateJobIterationResult( String testJob, TestJobResult testJobResult )
   {
      result.addTestJobResult( testJob, testJobResult );
   }

   private synchronized void accumulateIterationResult( HashMap<String, ScenarioResult> iterationResult )
   {
      result.addIterationResult( iterationResult );
   }

   @Builder.Default
   private ArrayList<Long> runningJobs = new ArrayList<Long>();

   private boolean deleteJobs()
   {
      boolean deleteJobResults = QuartzJobScheduler.deleteJobs( runningJobs );

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
         String runName = runInfo.getRunName();
         HashSet<String> tags = runInfo.getTags();

         result = new FeatureResult();

         EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
         KartaMinionRegistry nodeRegistry = kartaRuntime.getNodeRegistry();

         Random random = kartaRuntime.getRandom();

         boolean useMinions = kartaRuntime.getKartaConfiguration().isMinionsEnabled() && !nodeRegistry.getMinions().isEmpty();

         eventProcessor.raiseEvent( new FeatureStartEvent( runName, testFeature ) );

         if ( tags != null )
         {
            eventProcessor.featureStart( runName, testFeature, tags );
         }

         HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

         for ( TestJob job : testFeature.getTestJobs() )
         {
            try
            {
               long jobInterval = job.getInterval();
               int repeatCount = job.getIterationCount();

               if ( jobInterval > 0 )
               {
                  HashMap<String, Object> jobData = new HashMap<String, Object>();
                  jobData.put( Constants.KARTA_RUNTIME, kartaRuntime );
                  jobData.put( Constants.RUN_INFO, runInfo );
                  jobData.put( Constants.FEATURE_NAME, testFeature.getName() );
                  jobData.put( Constants.TEST_JOB, job );
                  jobData.put( Constants.ITERATION_COUNTER, new AtomicLong() );
                  TestJobIterationResultProcessor testJobIterationResultProcessor = ( jobName, testJobResult ) -> accumulateJobIterationResult( jobName, testJobResult );
                  jobData.put( Constants.TEST_JOB_ITERATION_RESULT_PROCESSOR, testJobIterationResultProcessor );
                  long jobId = QuartzJobScheduler.scheduleJob( QuartzTestJob.class, jobInterval, repeatCount, jobData );
                  runningJobs.add( jobId );
               }
               else
               {
                  TestJobRunner.run( kartaRuntime, runInfo, testFeature.getName(), job, 0 );
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

         HashMap<TestScenario, AtomicLong> scenarioIterationIndexMap = new HashMap<TestScenario, AtomicLong>();
         testFeature.getTestScenarios().forEach( ( scenario ) -> scenarioIterationIndexMap.put( scenario, new AtomicLong() ) );

         for ( TestStep step : testFeature.getSetupSteps() )
         {
            StepResult stepResult = new StepResult();
            stepResult.setSuccessful( true );

            eventProcessor.raiseEvent( new FeatureSetupStepStartEvent( runName, testFeature, step ) );

            try
            {
               stepResult = kartaRuntime.runStep( runInfo, testFeature.getName(), iterationIndex, Constants.__FEATURE_SETUP__, variables, testFeature.getTestDataSet(), step );
            }
            catch ( TestFailureException tfe )
            {
               log.error( "Exception when running step", tfe );
               stepResult.setSuccessful( false );
               TestIncident incident = TestIncident.builder().thrownCause( tfe ).build();
               stepResult.addIncident( incident );
            }
            finally
            {
               eventProcessor.raiseEvent( new FeatureSetupStepCompleteEvent( runName, testFeature, step, stepResult ) );

               result.getSetupResults().add( new SerializableKVP<String, Boolean>( step.getIdentifier(), stepResult.isPassed() ) );
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

         long numberOfIterations = runInfo.getNumberOfIterations();
         int numberOfIterationsInParallel = runInfo.getNumberOfIterationsInParallel();
         boolean chanceBasedScenarioExecution = runInfo.isChanceBasedScenarioExecution();
         boolean exclusiveScenarioPerIteration = runInfo.isExclusiveScenarioPerIteration();

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

            IterationRunner iterationRunner = IterationRunner.builder().kartaRuntime( kartaRuntime ).runInfo( runInfo ).featureName( testFeature.getName() ).commonTestDataSet( testFeature.getTestDataSet() )
                     .scenarioSetupSteps( testFeature.getScenarioSetupSteps() ).scenariosToRun( scenariosToRun ).scenarioTearDownSteps( testFeature.getScenarioTearDownSteps() ).iterationIndex( iterationIndex )
                     .scenarioIterationIndexMap( scenarioIterationIndexMap ).variables( DataUtils.cloneMap( variables ) ).resultConsumer( ( iterationResult ) -> accumulateIterationResult( iterationResult ) ).build();

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
         for ( TestStep step : testFeature.getTearDownSteps() )
         {
            StepResult stepResult = new StepResult();
            eventProcessor.raiseEvent( new FeatureTearDownStepStartEvent( runName, testFeature, step ) );

            try
            {
               stepResult = kartaRuntime.runStep( runInfo, testFeature.getName(), iterationIndex, Constants.__FEATURE_TEARDOWN__, variables, testFeature.getTestDataSet(), step );
            }
            catch ( TestFailureException tfe )
            {
               log.error( "Exception when running step", tfe );
               stepResult.setSuccessful( false );
               TestIncident incident = TestIncident.builder().thrownCause( tfe ).build();
               stepResult.addIncident( incident );
            }
            finally
            {
               eventProcessor.raiseEvent( new FeatureTearDownStepCompleteEvent( runName, testFeature, step, stepResult ) );

               result.getTearDownResults().add( new SerializableKVP<String, Boolean>( step.getIdentifier(), stepResult.isPassed() ) );
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

         if ( tags != null )
         {
            eventProcessor.featureStop( runName, testFeature, tags );
         }

         eventProcessor.raiseEvent( new FeatureCompleteEvent( runName, testFeature, result ) );
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
