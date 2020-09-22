package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;

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
   @Builder.Default
   @PropertyMapping( propertyName = "RunProperties" )
   private RunProperties                                  runProperties = new RunProperties();

   private StepRunner                                     stepRunner;
   private ArrayList<TestDataSource>                      testDataSources;
   private HashMap<String, HashMap<String, Serializable>> testProperties;

   public boolean run( TestFeature testFeature )
   {
      try
      {
         HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
         HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

         TestExecutionContext testExecutionContext = new TestExecutionContext( testProperties, testData, variables );

         long iterationIndex = -1;
         long stepIndex = 0;

         for ( TestStep step : testFeature.getTestSetupSteps() )
         {
            testData = KartaRuntime.getMergedTestData( testDataSources, new ExecutionStepPointer( testFeature.getName(), null, step, iterationIndex, stepIndex++ ) );
            // log.debug( "Step test data is " + testData.toString() );
            testExecutionContext.setTestData( testData );

            boolean stepResult = false;

            try
            {
               stepResult = stepRunner.runStep( step, testExecutionContext );
            }
            catch ( TestFailureException tfe )
            {
               log.error( "Exception in test failure ", tfe );
            }
            finally
            {
               if ( !stepResult )
               {
                  log.error( "Feature \"" + testFeature.getName() + "\" failed at setup step " + step );
                  return true;
               }
            }
         }

         iterationIndex = 0;

         nextScenario: for ( TestScenario testScenario : testFeature.getTestScenarios() )
         {
            log.debug( "Running Scenario: " + testScenario );

            try
            {
               for ( TestStep step : Stream.concat( testFeature.getScenarioSetupSteps().stream(), testScenario.getScenarioSetupSteps().stream() ).collect( Collectors.toList() ) )
               {
                  testData = KartaRuntime.getMergedTestData( testDataSources, new ExecutionStepPointer( testFeature.getName(), testScenario.getName(), step, iterationIndex, stepIndex++ ) );
                  // log.debug( "Step test data is " + testData.toString() );
                  testExecutionContext.setTestData( testData );

                  if ( !stepRunner.runStep( step, testExecutionContext ) )
                  {
                     log.error( "Scenario \"" + testScenario.getName() + "\" failed at setup step " + step );
                     continue nextScenario;
                  }
               }

               for ( TestStep step : testScenario.getScenarioExecutionSteps() )
               {
                  testData = KartaRuntime.getMergedTestData( testDataSources, new ExecutionStepPointer( testFeature.getName(), testScenario.getName(), step, iterationIndex, stepIndex++ ) );
                  // log.debug( "Step test data is " + testData.toString() );
                  testExecutionContext.setTestData( testData );

                  if ( !stepRunner.runStep( step, testExecutionContext ) )
                  {
                     log.error( "Scenario \"" + testScenario.getName() + "\" failed at step " + step );
                     continue nextScenario;
                  }
               }
            }
            catch ( Throwable t )
            {

            }
            finally
            {
               for ( TestStep step : Stream.concat( testFeature.getScenarioTearDownSteps().stream(), testScenario.getScenarioTearDownSteps().stream() ).collect( Collectors.toList() ) )
               {
                  testData = KartaRuntime.getMergedTestData( testDataSources, new ExecutionStepPointer( testFeature.getName(), testScenario.getName(), step, iterationIndex, stepIndex++ ) );
                  // log.debug( "Step test data is " + testData.toString() );
                  testExecutionContext.setTestData( testData );

                  try
                  {
                     if ( !stepRunner.runStep( step, testExecutionContext ) )
                     {
                        log.error( "Scenario \"" + testScenario.getName() + "\" failed at teardown step " + step );
                        // break;
                     }
                  }
                  catch ( Throwable t )
                  {
                     // TODO: Ignore teardown failure and continue to next scenario
                     log.error( t );
                     continue nextScenario;
                  }
               }
            }
         }

         for ( TestStep step : testFeature.getTestTearDownSteps() )
         {
            testData = KartaRuntime.getMergedTestData( testDataSources, new ExecutionStepPointer( testFeature.getName(), null, step, iterationIndex, stepIndex++ ) );
            // log.debug( "Step test data is " + testData.toString() );
            testExecutionContext.setTestData( testData );

            boolean stepResult = false;

            try
            {
               stepResult = stepRunner.runStep( step, testExecutionContext );
            }
            catch ( TestFailureException tfe )
            {
               log.error( "Exception in test failure ", tfe );
            }
            finally
            {
               if ( !stepResult )
               {
                  log.error( "Feature \"" + testFeature.getName() + "\" failed at downdown " + step );
                  continue;
               }
            }
         }
      }
      catch ( ClassNotFoundException cnfe )
      {
         log.error( "Plugin class load exception", cnfe );
         return false;
      }
      catch ( Throwable t )
      {
         log.error( "Exception occured when trying to run test", t );
         return false;
      }
      return true;
   }
}
