package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.interfaces.FeatureSourceParser;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;
import org.mvss.karta.framework.utils.ClassPathLoaderUtils;

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

   private String                                         defaultFeatureSourceParserPlugin;
   private String                                         defaultStepRunnerPlugin;
   private HashSet<String>                                defaultTestDataSourcePlugins;

   private HashMap<String, HashMap<String, Serializable>> testProperties;

   private static PnPRegistry                             pnpRegistry   = KartaRuntime.getInstance().getPnPRegistry();

   public HashMap<String, Serializable> getMergedTestData( ArrayList<TestDataSource> testDataSources, ExecutionStepPointer executionStepPointer ) throws Throwable
   {
      HashMap<String, Serializable> mergedTestData = new HashMap<String, Serializable>();

      for ( TestDataSource tds : testDataSources )
      {
         HashMap<String, Serializable> testData = tds.getData( executionStepPointer );
         testData.forEach( ( key, value ) -> mergedTestData.put( key, value ) );
      }

      return mergedTestData;
   }

   public boolean runFeatureFile( String featureFileName )
   {
      try
      {
         return runFeatureSource( ClassPathLoaderUtils.readAllText( featureFileName ) );
      }
      catch ( Throwable t )
      {
         log.error( t );
         return false;
      }
   }

   public boolean runFeatureSource( String featureFileSourceString )
   {
      try
      {
         FeatureSourceParser featureParser = (FeatureSourceParser) pnpRegistry.getPlugin( defaultFeatureSourceParserPlugin, FeatureSourceParser.class );

         if ( featureParser == null )
         {
            log.error( "Failed to get a feature source parser of type: " + defaultFeatureSourceParserPlugin );
            return false;
         }
         TestFeature testFeature = featureParser.parseFeatureSource( featureFileSourceString );

         return run( testFeature );
      }
      catch ( Throwable t )
      {
         log.error( t );
         return false;
      }
   }

   public boolean run( TestFeature testFeature )
   {
      try
      {
         StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( defaultStepRunnerPlugin, StepRunner.class );

         if ( stepRunner == null )
         {
            log.error( "Failed to get a step runner of type: " + defaultStepRunnerPlugin );
            return false;
         }

         ArrayList<TestDataSource> testDataSources = new ArrayList<TestDataSource>();

         for ( String testDataSourcePlugin : defaultTestDataSourcePlugins )
         {
            TestDataSource testDataSource = (TestDataSource) pnpRegistry.getPlugin( testDataSourcePlugin, TestDataSource.class );

            if ( testDataSource == null )
            {
               log.error( "Failed to get a test data source of type: " + testDataSourcePlugin );
               return false;
            }

            testDataSources.add( testDataSource );
         }

         HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
         HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

         TestExecutionContext testExecutionContext = new TestExecutionContext( testProperties, testData, variables );

         long iterationIndex = -1;
         long stepIndex = 0;

         for ( TestStep step : testFeature.getTestSetupSteps() )
         {
            testData = getMergedTestData( testDataSources, new ExecutionStepPointer( testFeature.getName(), null, step, iterationIndex, stepIndex++ ) );
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
                  testData = getMergedTestData( testDataSources, new ExecutionStepPointer( testFeature.getName(), testScenario.getName(), step, iterationIndex, stepIndex++ ) );
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
                  testData = getMergedTestData( testDataSources, new ExecutionStepPointer( testFeature.getName(), testScenario.getName(), step, iterationIndex, stepIndex++ ) );
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
                  testData = getMergedTestData( testDataSources, new ExecutionStepPointer( testFeature.getName(), testScenario.getName(), step, iterationIndex, stepIndex++ ) );
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
            testData = getMergedTestData( testDataSources, new ExecutionStepPointer( testFeature.getName(), null, step, iterationIndex, stepIndex++ ) );
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
