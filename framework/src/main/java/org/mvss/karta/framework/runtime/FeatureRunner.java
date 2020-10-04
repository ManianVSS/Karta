package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.minions.KartaMinionRegistry;
import org.mvss.karta.framework.randomization.RandomizationUtils;
import org.mvss.karta.framework.runtime.event.EventProcessor;
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
   private static Random                                  random = new Random();

   private StepRunner                                     stepRunner;
   private ArrayList<TestDataSource>                      testDataSources;
   private HashMap<String, HashMap<String, Serializable>> testProperties;
   private EventProcessor                                 eventProcessor;
   private KartaMinionRegistry                            minionRegistry;

   public boolean run( String runName, TestFeature testFeature ) throws Throwable
   {
      return run( runName, testFeature, 1, 1 );
   }

   public boolean run( String runName, TestFeature testFeature, long numberOfIterations, int numberOfIterationsInParallel ) throws Throwable
   {
      eventProcessor.raiseFeatureStartedEvent( runName, testFeature );

      HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
      HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

      TestExecutionContext testExecutionContext = new TestExecutionContext( testProperties, testData, variables );

      long iterationIndex = -1;
      long stepIndex = 0;

      stepIndex = 0;
      for ( TestStep step : testFeature.getSetupSteps() )
      {
         testData = KartaRuntime.getMergedTestData( testDataSources, new ExecutionStepPointer( testFeature.getName(), null, step, iterationIndex, stepIndex++ ) );
         // log.debug( "Step test data is " + testData.toString() );
         testExecutionContext.setData( testData );

         StepResult stepResult = new StepResult();

         eventProcessor.raiseFeatureSetupStepStartedEvent( runName, testFeature, step );

         try
         {
            if ( StringUtils.isNotEmpty( step.getNode() ) )
            {
               stepResult = minionRegistry.getMinion( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
               DataUtils.mergeVariables( stepResult.getVariables(), testExecutionContext.getVariables() );
            }
            else
            {
               stepResult = stepRunner.runStep( step, testExecutionContext );
            }
         }
         catch ( TestFailureException tfe )
         {
            log.error( "Exception in test failure ", tfe );
            stepResult.setMessage( tfe.getMessage() );
            stepResult.setErrorThrown( tfe );
         }
         finally
         {
            eventProcessor.raiseFeatureSetupStepCompletedEvent( runName, testFeature, step, stepResult );

            if ( !stepResult.isSuccesssful() )
            {
               // log.error( "Feature \"" + testFeature.getName() + "\" failed at setup step " + step );
               eventProcessor.raiseFeatureCompletedEvent( runName, testFeature );
               return false;
            }
         }
      }

      // TODO Check for valid number number of iterations

      // TODO check numberOfIterationsInParallel for invalid values

      ExecutorService iterationExecutionService = new ThreadPoolExecutor( numberOfIterationsInParallel, numberOfIterationsInParallel, 0L, TimeUnit.MILLISECONDS, new BlockingRunnableQueue( numberOfIterationsInParallel ) );

      for ( iterationIndex = 0; iterationIndex < numberOfIterations; iterationIndex++ )
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

         IterationRunner iterationRunner = IterationRunner.builder().stepRunner( stepRunner ).testDataSources( testDataSources ).testProperties( testProperties ).feature( testFeature ).runName( runName ).eventProcessor( eventProcessor )
                  .minionRegistry( minionRegistry ).scenariosToRun( scenariosToRun ).iterationIndex( iterationIndex ).build();

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

      iterationIndex = -1;
      stepIndex = 0;
      for ( TestStep step : testFeature.getTearDownSteps() )
      {
         testData = KartaRuntime.getMergedTestData( testDataSources, new ExecutionStepPointer( testFeature.getName(), null, step, iterationIndex, stepIndex++ ) );
         // log.debug( "Step test data is " + testData.toString() );
         testExecutionContext.setData( testData );

         StepResult stepResult = new StepResult();

         eventProcessor.raiseFeatureTearDownStepStartedEvent( runName, testFeature, step );

         try
         {
            if ( StringUtils.isNotEmpty( step.getNode() ) )
            {
               stepResult = minionRegistry.getMinion( step.getNode() ).runStep( stepRunner.getPluginName(), step, testExecutionContext );
               DataUtils.mergeVariables( stepResult.getVariables(), testExecutionContext.getVariables() );
            }
            else
            {
               stepResult = stepRunner.runStep( step, testExecutionContext );
            }
         }
         catch ( TestFailureException tfe )
         {
            log.error( "Exception in test failure ", tfe );
            stepResult.setMessage( tfe.getMessage() );
            stepResult.setErrorThrown( tfe );
         }
         finally
         {
            eventProcessor.raiseFeatureTearDownStepCompleteEvent( runName, testFeature, step, stepResult );

            if ( !stepResult.isSuccesssful() )
            {
               // log.error( "Feature \"" + testFeature.getName() + "\" failed at teardown " + step );
               continue;
            }
         }
      }

      eventProcessor.raiseFeatureCompletedEvent( runName, testFeature );

      return true;
   }
}
