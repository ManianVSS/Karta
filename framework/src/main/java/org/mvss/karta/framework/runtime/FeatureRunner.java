package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
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
public class FeatureRunner
{
   private static Random             random = new Random();

   private KartaRuntime              kartaRuntime;
   private StepRunner                stepRunner;
   private ArrayList<TestDataSource> testDataSources;

   public boolean run( String runName, TestFeature testFeature ) throws Throwable
   {
      return run( runName, testFeature, 1, 1 );
   }

   public boolean run( String runName, TestFeature testFeature, long numberOfIterations, int numberOfIterationsInParallel ) throws Throwable
   {
      EventProcessor eventProcessor = kartaRuntime.getEventProcessor();
      HashMap<String, HashMap<String, Serializable>> testProperties = kartaRuntime.getConfigurator().getPropertiesStore();
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
         TestExecutionContext testExecutionContext = new TestExecutionContext( runName, testFeature.getName(), iterationIndex, Constants.FEATURE_SETUP, step.getIdentifier(), testProperties, testData, variables );

         testData = KartaRuntime
                  .getMergedTestData( runName, step.getTestData(), testDataSources, new ExecutionStepPointer( testFeature.getName(), Constants.FEATURE_SETUP, stepRunner.sanitizeStepDefinition( step.getIdentifier() ), iterationIndex, stepIndex++ ) );
         // log.debug( "Step test data is " + testData.toString() );
         testExecutionContext.setData( testData );

         StepResult stepResult = new StepResult();

         eventProcessor.raiseEvent( new FeatureSetupStepStartEvent( runName, testFeature, step ) );

         try
         {
            if ( StringUtils.isNotEmpty( step.getNode() ) )
            {
               stepResult = nodeRegistry.getNode( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
               DataUtils.mergeVariables( stepResult.getResults(), testExecutionContext.getVariables() );
            }
            else
            {
               stepResult = stepRunner.runStep( step, testExecutionContext );
            }
         }
         catch ( TestFailureException tfe )
         {
            log.error( "Exception in test failure ", tfe );
            stepResult.setSuccesssful( false );
            stepResult.addIncident( TestIncident.builder().thrownCause( tfe ).build() );
         }
         finally
         {
            eventProcessor.raiseEvent( new FeatureSetupStepCompleteEvent( runName, testFeature, step, stepResult ) );

            if ( !stepResult.isSuccesssful() )
            {
               // log.error( "Feature \"" + testFeature.getName() + "\" failed at setup step " + step );
               if ( !QuartzJobScheduler.deleteJobs( runningJobs ) )
               {
                  log.error( "Failed to delete test jobs" );
               }

               eventProcessor.raiseEvent( new FeatureCompleteEvent( runName, testFeature ) );
               return false;
            }
         }
      }

      // TODO Check for valid number number of iterations

      // TODO check numberOfIterationsInParallel for invalid values

      ExecutorService iterationExecutionService = new ThreadPoolExecutor( numberOfIterationsInParallel, numberOfIterationsInParallel, 0L, TimeUnit.MILLISECONDS, new BlockingRunnableQueue( numberOfIterationsInParallel ) );

      for ( iterationIndex = 0; ( numberOfIterations <= 0 ) || ( iterationIndex < numberOfIterations ); iterationIndex++ )
      {
         ArrayList<TestScenario> scenariosToRun = new ArrayList<TestScenario>();

         if ( testFeature.getChanceBasedScenarioExecution() )
         {
            if ( testFeature.getExclusiveScenarioPerIteration() )
            {
               scenariosToRun.add( RandomizationUtils.generateNextMutexComposition( random, testFeature.getTestScenarios() ) );
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
                  .iterationIndex( iterationIndex ).scenarioIterationIndexMap( scenarioIterationIndexMap ).build();

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
         TestExecutionContext testExecutionContext = new TestExecutionContext( runName, testFeature.getName(), iterationIndex, Constants.FEATURE_TEARDOWN, step.getIdentifier(), testProperties, testData, variables );

         testData = KartaRuntime
                  .getMergedTestData( runName, step.getTestData(), testDataSources, new ExecutionStepPointer( testFeature.getName(), Constants.FEATURE_TEARDOWN, stepRunner.sanitizeStepDefinition( step.getIdentifier() ), iterationIndex, stepIndex++ ) );
         // log.debug( "Step test data is " + testData.toString() );
         testExecutionContext.setData( testData );

         StepResult stepResult = new StepResult();

         eventProcessor.raiseEvent( new FeatureTearDownStepStartEvent( runName, testFeature, step ) );

         try
         {
            if ( StringUtils.isNotEmpty( step.getNode() ) )
            {
               stepResult = nodeRegistry.getNode( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
               DataUtils.mergeVariables( stepResult.getResults(), testExecutionContext.getVariables() );
            }
            else
            {
               stepResult = stepRunner.runStep( step, testExecutionContext );
            }
         }
         catch ( TestFailureException tfe )
         {
            log.error( "Exception in test failure ", tfe );
            stepResult.setSuccesssful( false );
            stepResult.addIncident( TestIncident.builder().thrownCause( tfe ).build() );
         }
         finally
         {
            eventProcessor.raiseEvent( new FeatureTearDownStepCompleteEvent( runName, testFeature, step, stepResult ) );

            if ( !stepResult.isSuccesssful() )
            {
               // log.error( "Feature \"" + testFeature.getName() + "\" failed at teardown " + step );
               continue;
            }
         }
      }

      eventProcessor.raiseEvent( new FeatureCompleteEvent( runName, testFeature ) );

      if ( !QuartzJobScheduler.deleteJobs( runningJobs ) )
      {
         log.error( "Failed to delete test jobs" );
      }

      return true;
   }
}
