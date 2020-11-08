package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.minions.KartaMinionRegistry;
import org.mvss.karta.framework.randomization.RandomizationUtils;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.FeatureCompleteEvent;
import org.mvss.karta.framework.runtime.event.FeatureSetupStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.FeatureSetupStepStartEvent;
import org.mvss.karta.framework.runtime.event.FeatureStartEvent;
import org.mvss.karta.framework.runtime.event.FeatureTearDownStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.FeatureTearDownStepStartEvent;
import org.mvss.karta.framework.runtime.event.TestIncidentOccurenceEvent;
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
public class FeatureRunner implements Callable<Boolean>
{
   private static Random             random                        = new Random();

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

   @Builder.Default
   private boolean                   successful                    = true;

   @Override
   public Boolean call()
   {
      try
      {
         successful = true;
         // testFeature = testFeature.getFeatureMergedWithCommonSteps();

         EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
         KartaMinionRegistry nodeRegistry = kartaRuntime.getNodeRegistry();

         ArrayList<Integer> runningJobs = new ArrayList<Integer>();

         eventProcessor.raiseEvent( new FeatureStartEvent( runName, testFeature ) );

         HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
         HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

         for ( TestJob job : testFeature.getTestJobs() )
         {
            long jobInterval = job.getInterval();

            if ( jobInterval > 0 )
            {
               HashMap<String, Object> jobData = new HashMap<String, Object>();
               jobData.put( "kartaRuntime", kartaRuntime );
               jobData.put( "stepRunner", stepRunner );
               jobData.put( "testDataSources", testDataSources );
               jobData.put( "runName", runName );
               jobData.put( "testFeature", testFeature );
               jobData.put( "testJob", job );
               jobData.put( "iterationCounter", new AtomicInteger() );
               runningJobs.add( QuartzJobScheduler.scheduleJob( QuartzTestJob.class, jobInterval, jobData ) );
            }
            else
            {
               TestJobRunner.run( kartaRuntime, stepRunner, testDataSources, runName, testFeature, job, 0 );
            }
         }

         int iterationIndex = -1;
         int stepIndex = 0;

         HashMap<TestScenario, AtomicInteger> scenarioIterationIndexMap = new HashMap<TestScenario, AtomicInteger>();
         testFeature.getTestScenarios().forEach( ( scenario ) -> scenarioIterationIndexMap.put( scenario, new AtomicInteger() ) );

         stepIndex = 0;
         for ( TestStep step : testFeature.getSetupSteps() )
         {
            TestExecutionContext testExecutionContext = new TestExecutionContext( runName, testFeature.getName(), iterationIndex, Constants.__FEATURE_SETUP__, step.getIdentifier(), testData, variables );

            testData = KartaRuntime.getMergedTestData( runName, step.getTestData(), step.getTestDataSet(), testDataSources, new ExecutionStepPointer( testFeature.getName(), Constants.__FEATURE_SETUP__,
                                                                                                                                                      stepRunner.sanitizeStepDefinition( step.getIdentifier() ), iterationIndex, stepIndex++ ) );
            // log.debug( "Step test data is " + testData.toString() );
            testExecutionContext.setData( testData );

            StepResult stepResult = new StepResult();
            stepResult.setSuccesssful( true );

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
                  eventProcessor.raiseEvent( new TestIncidentOccurenceEvent( runName, testFeature.getName(), iterationIndex, Constants.__FEATURE_SETUP__, step.getIdentifier(), incident ) );
               }

               DataUtils.mergeVariables( stepResult.getResults(), variables );
            }
            catch ( TestFailureException tfe )
            {
               log.error( "Exception in test failure ", tfe );
               stepResult.setSuccesssful( false );
               TestIncident incident = TestIncident.builder().thrownCause( tfe ).build();
               stepResult.addIncident( incident );
            }
            finally
            {
               eventProcessor.raiseEvent( new FeatureSetupStepCompleteEvent( runName, testFeature, step, stepResult ) );

               if ( !stepResult.isSuccesssful() )
               {
                  successful = false;

                  if ( !QuartzJobScheduler.deleteJobs( runningJobs ) )
                  {
                     log.error( "Failed to delete test jobs" );
                     successful = false;
                  }

                  eventProcessor.raiseEvent( new FeatureCompleteEvent( runName, testFeature ) );
                  return successful;
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

            IterationRunner iterationRunner = IterationRunner.builder().kartaRuntime( kartaRuntime ).stepRunner( stepRunner ).testDataSources( testDataSources ).feature( testFeature ).runName( runName ).scenariosToRun( scenariosToRun )
                     .iterationIndex( iterationIndex ).scenarioIterationIndexMap( scenarioIterationIndexMap ).variables( DataUtils.cloneMap( variables ) ).build();

            if ( numberOfIterationsInParallel == 1 )
            {
               log.debug( "Iteration start " + iterationIndex + " with scenarios " + scenariosToRun );
               iterationRunner.run();
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
                  eventProcessor.raiseEvent( new TestIncidentOccurenceEvent( runName, testFeature.getName(), iterationIndex, Constants.__FEATURE_SETUP__, step.getIdentifier(), incident ) );
               }

               DataUtils.mergeVariables( stepResult.getResults(), variables );
            }
            catch ( TestFailureException tfe )
            {
               log.error( "Exception in test failure ", tfe );
               stepResult.setSuccesssful( false );
               TestIncident incident = TestIncident.builder().thrownCause( tfe ).build();
               stepResult.addIncident( incident );
            }
            finally
            {
               eventProcessor.raiseEvent( new FeatureTearDownStepCompleteEvent( runName, testFeature, step, stepResult ) );

               if ( !stepResult.isSuccesssful() )
               {
                  successful = false;
                  continue;
               }
            }
         }

         eventProcessor.raiseEvent( new FeatureCompleteEvent( runName, testFeature ) );

         if ( !QuartzJobScheduler.deleteJobs( runningJobs ) )
         {
            log.error( "Failed to delete test jobs" );
            successful = false;
         }
      }
      catch ( Throwable t )
      {
         log.error( t );
         log.error( ExceptionUtils.getStackTrace( t ) );
         successful = false;
      }
      return successful;
   }
}
