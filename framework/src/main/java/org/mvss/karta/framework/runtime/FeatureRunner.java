package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
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
import org.mvss.karta.framework.nodes.KartaNode;
import org.mvss.karta.framework.nodes.KartaNodeRegistry;
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
import org.mvss.karta.framework.utils.WaitUtil;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Runner class to run TestFeatures for Karta
 * 
 * @author Manian
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
@Builder
public class FeatureRunner implements Callable<FeatureResult>
{
   private KartaRuntime            kartaRuntime;

   private RunInfo                 runInfo;

   private TestFeature             testFeature;

   /**
    * The call back for updating execution result of the TestFeature after run completes.
    */
   private Consumer<FeatureResult> resultConsumer;

   private FeatureResult           result;

   /**
    * The callback implementation for job iteration result updates for running Test Feature
    * 
    * @param testJob
    * @param testJobResult
    */
   private synchronized void accumulateJobIterationResult( String testJob, TestJobResult testJobResult )
   {
      result.addTestJobResult( testJob, testJobResult );
   }

   /**
    * The callback implementation for feature iteration result updates for running Test Feature
    * 
    * @param iterationResult
    */
   private synchronized void accumulateIterationResult( HashMap<String, ScenarioResult> iterationResult )
   {
      result.addIterationResult( iterationResult );
   }

   @Builder.Default
   private ArrayList<Long> runningJobs = new ArrayList<Long>();

   private void deleteJobs()
   {
      boolean deleteJobResults = QuartzJobScheduler.deleteJobs( runningJobs );

      if ( !deleteJobResults )
      {
         log.error( "Failed to delete test jobs" );
         result.setSuccessful( false );
      }
   }

   public void updateResultCallBack()
   {
      result.setEndTime( new Date() );
      if ( resultConsumer != null )
      {
         resultConsumer.accept( result );
      }
   }

   /**
    * After initializing calling this method would run the feature.
    * Call implementation for asynchronous calling.
    */
   @Override
   public FeatureResult call()
   {
      try
      {
         String runName = runInfo.getRunName();
         HashSet<String> tags = runInfo.getTags();

         result = new FeatureResult();
         result.setFeatureName( testFeature.getName() );
         EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
         KartaNodeRegistry nodeRegistry = kartaRuntime.getNodeRegistry();
         BeanRegistry contextBeanRegistry = new BeanRegistry();

         Random random = kartaRuntime.getRandom();

         boolean useMinions = kartaRuntime.getKartaConfiguration().isMinionsEnabled() && !nodeRegistry.getMinions().isEmpty();

         eventProcessor.raiseEvent( new FeatureStartEvent( runName, testFeature ) );

         if ( tags != null )
         {
            if ( !eventProcessor.featureStart( runName, testFeature, tags ) )
            {
               eventProcessor.featureStop( runName, testFeature, tags );
               result.setError( true );
               updateResultCallBack();
               return result;
            }
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
                  jobData.put( Constants.BEAN_REGISTRY, contextBeanRegistry );
                  long jobId = QuartzJobScheduler.scheduleJob( QuartzTestJob.class, jobInterval, repeatCount, jobData );
                  runningJobs.add( jobId );
               }
               else
               {
                  TestJobRunner.run( kartaRuntime, runInfo, testFeature.getName(), job, 0, contextBeanRegistry );
               }
            }
            catch ( Throwable t )
            {
               log.error( "Exception occured while scheduling jobs ", t );
               deleteJobs();
            }
         }

         long iterationIndex = -1;

         HashMap<TestScenario, AtomicLong> scenarioIterationIndexMap = new HashMap<TestScenario, AtomicLong>();
         testFeature.getTestScenarios().forEach( ( scenario ) -> scenarioIterationIndexMap.put( scenario, new AtomicLong() ) );

         long setupStepIndex = -1;
         for ( TestStep step : testFeature.getSetupSteps() )
         {
            setupStepIndex++;
            StepResult stepResult = new StepResult();
            stepResult.setStepIndex( setupStepIndex );
            stepResult.setSuccessful( true );

            eventProcessor.raiseEvent( new FeatureSetupStepStartEvent( runName, testFeature, step ) );

            try
            {
               stepResult = kartaRuntime.runStep( runInfo, testFeature.getName(), iterationIndex, Constants.__FEATURE_SETUP__, variables, testFeature.getTestDataSet(), step, contextBeanRegistry );
               stepResult.setStepIndex( setupStepIndex );
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

               result.getSetupResults().add( new SerializableKVP<String, StepResult>( step.getStep(), stepResult ) );
               result.getIncidents().addAll( stepResult.getIncidents() );

               if ( !stepResult.isPassed() )
               {
                  result.setSuccessful( false );
                  deleteJobs();

                  eventProcessor.raiseEvent( new FeatureCompleteEvent( runName, testFeature, result ) );

                  if ( tags != null )
                  {
                     if ( !eventProcessor.featureStop( runName, testFeature, tags ) )
                     {
                        result.setError( true );
                     }
                  }

                  updateResultCallBack();
                  return result;
               }
            }
         }

         long numberOfIterations = runInfo.getNumberOfIterations();
         int numberOfIterationsInParallel = runInfo.getNumberOfIterationsInParallel();
         boolean chanceBasedScenarioExecution = runInfo.isChanceBasedScenarioExecution();
         boolean exclusiveScenarioPerIteration = runInfo.isExclusiveScenarioPerIteration();
         Duration targetRunDuration = runInfo.getRunDuration();
         Duration coolDownBetweenDuration = runInfo.getCoolDownBetweenIterations();

         if ( !DataUtils.inRange( numberOfIterations, 0, Integer.MAX_VALUE ) )
         {
            log.error( "Configuration error: invalid number of iterations: " + numberOfIterations );
            numberOfIterations = 0;
         }

         if ( !DataUtils.inRange( numberOfIterationsInParallel, 1, Integer.MAX_VALUE ) )
         {
            log.error( "Configuration error: invalid number of threads: " + numberOfIterationsInParallel );
            numberOfIterationsInParallel = 1;
         }

         ExecutorService iterationExecutionService = null;

         if ( numberOfIterationsInParallel > 1 )
         {
            iterationExecutionService = new ThreadPoolExecutor( numberOfIterationsInParallel, numberOfIterationsInParallel, 0L, TimeUnit.MILLISECONDS, new BlockingRunnableQueue( numberOfIterationsInParallel ) );
         }

         Instant startTime = Instant.now();

         for ( iterationIndex = 0; ( numberOfIterations <= 0 ) || ( iterationIndex < numberOfIterations ); iterationIndex++ )
         {
            // Break on target Run Duration
            if ( targetRunDuration != null )
            {
               if ( targetRunDuration.compareTo( Duration.between( startTime, Instant.now() ) ) <= 0 )
               {
                  break;
               }
            }

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
               KartaNode minion = nodeRegistry.getNextMinion();

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

            if ( coolDownBetweenDuration != null )
            {
               if ( iterationIndex % numberOfIterationsInParallel == 0 )
               {
                  WaitUtil.sleep( coolDownBetweenDuration.toMillis() );
               }
            }
         }

         if ( numberOfIterationsInParallel > 1 )
         {
            iterationExecutionService.shutdown();
            iterationExecutionService.awaitTermination( Long.MAX_VALUE, TimeUnit.NANOSECONDS );
         }

         testFeature.getTestScenarios().forEach( ( scenario ) -> scenarioIterationIndexMap.get( scenario ).set( 0 ) );

         long teardownStepIndex = -1;
         iterationIndex = -1;
         for ( TestStep step : testFeature.getTearDownSteps() )
         {
            teardownStepIndex++;
            StepResult stepResult = new StepResult();
            stepResult.setStepIndex( teardownStepIndex );
            eventProcessor.raiseEvent( new FeatureTearDownStepStartEvent( runName, testFeature, step ) );

            try
            {
               stepResult = kartaRuntime.runStep( runInfo, testFeature.getName(), iterationIndex, Constants.__FEATURE_TEARDOWN__, variables, testFeature.getTestDataSet(), step, contextBeanRegistry );
               stepResult.setStepIndex( teardownStepIndex );
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

               result.getTearDownResults().add( new SerializableKVP<String, StepResult>( step.getStep(), stepResult ) );
               result.getIncidents().addAll( stepResult.getIncidents() );

               if ( !stepResult.isPassed() )
               {
                  result.setSuccessful( false );
                  continue;
               }
            }
         }

         deleteJobs();

         if ( tags != null )
         {
            if ( !eventProcessor.featureStop( runName, testFeature, tags ) )
            {
               result.setError( true );
            }
         }

         eventProcessor.raiseEvent( new FeatureCompleteEvent( runName, testFeature, result ) );
      }
      catch ( Throwable t )
      {
         log.error( "Exception occured during feature run", t );
         log.error( ExceptionUtils.getStackTrace( t ) );
         result.setError( true );
      }

      updateResultCallBack();
      return result;
   }
}
