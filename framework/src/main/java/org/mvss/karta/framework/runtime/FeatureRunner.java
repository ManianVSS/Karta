package org.mvss.karta.framework.runtime;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.configuration.PluginClassConfig;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.interfaces.FeatureSourceParser;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;
import org.mvss.karta.framework.utils.ClassPathLoaderUtils;
import org.mvss.karta.framework.utils.ExtensionLoader;

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
public class FeatureRunner implements Runnable
{
   private static final ExtensionLoader<FeatureSourceParser> featureParserClassLoader  = new ExtensionLoader<FeatureSourceParser>();
   private PluginClassConfig                                 featureSourceParserConfig;

   private static final ExtensionLoader<StepRunner>          stepRunnerClassLoader     = new ExtensionLoader<StepRunner>();
   private PluginClassConfig                                 stepRunnerConfig;

   private static final ExtensionLoader<TestDataSource>      testDataSourceClassLoader = new ExtensionLoader<TestDataSource>();
   private PluginClassConfig                                 testDataSourceConfig;

   private String                                            featureFile;

   @SuppressWarnings( "unchecked" )
   @Override
   public void run()
   {
      try
      {
         Class<? extends FeatureSourceParser> featureParserClass = StringUtils.isNotBlank( featureSourceParserConfig.getJarFile() )
                  ? featureParserClassLoader.LoadClass( new File( featureSourceParserConfig.getJarFile() ), featureSourceParserConfig.getClassName() )
                  : (Class<? extends FeatureSourceParser>) Class.forName( featureSourceParserConfig.getClassName() );
         FeatureSourceParser featureParser = featureParserClass.newInstance();
         featureParser.initFeatureParser( featureSourceParserConfig.getProperties() );

         Class<? extends TestDataSource> testDataSourceClass = StringUtils.isNotBlank( testDataSourceConfig.getJarFile() ) ? testDataSourceClassLoader.LoadClass( new File( testDataSourceConfig.getJarFile() ), testDataSourceConfig.getClassName() )
                  : (Class<? extends TestDataSource>) Class.forName( testDataSourceConfig.getClassName() );
         TestDataSource testDataSource = testDataSourceClass.newInstance();
         testDataSource.initDataSource( testDataSourceConfig.getProperties() );

         Class<? extends StepRunner> stepRunnerClass = StringUtils.isNotBlank( stepRunnerConfig.getJarFile() ) ? stepRunnerClassLoader.LoadClass( new File( stepRunnerConfig.getJarFile() ), stepRunnerConfig.getClassName() )
                  : (Class<? extends StepRunner>) Class.forName( stepRunnerConfig.getClassName() );
         StepRunner stepRunner = stepRunnerClass.newInstance();
         stepRunner.initStepRepository( stepRunnerConfig.getProperties() );

         String featureFileSourceString = ClassPathLoaderUtils.readAllText( featureFile );
         TestFeature testFeature = featureParser.parseFeatureSource( featureFileSourceString );

         HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
         HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

         TestExecutionContext testExecutionContext = new TestExecutionContext( stepRunnerConfig.getProperties(), testData, variables );

         long iterationIndex = -1;
         long stepIndex = 0;

         for ( TestStep step : testFeature.getTestSetupSteps() )
         {
            testData = testDataSource.getData( new ExecutionStepPointer( testFeature.getName(), null, step, iterationIndex, stepIndex++ ) );
            log.debug( "Step test data is " + testData.toString() );
            testExecutionContext.setTestData( testData );

            // if ( !stepRunner.runStep( step, testExecutionContext ) )
            // {
            // log.error( "Feature setup failed at step " + step );
            // return;
            // }

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
                  log.error( "Feature " + testFeature + " failed at setup step " + step );
                  return;
               }
            }
         }

         iterationIndex = 0;

         nextScenario: for ( TestScenario testScenario : testFeature.getTestScenarios() )
         {
            log.debug( "Running Scenario: " + testScenario );

            try
            {
               for ( TestStep step : testFeature.getScenarioSetupSteps() )
               {
                  testData = testDataSource.getData( new ExecutionStepPointer( testFeature.getName(), testScenario.getName(), step, iterationIndex, stepIndex++ ) );
                  log.debug( "Step test data is " + testData.toString() );
                  testExecutionContext.setTestData( testData );

                  if ( !stepRunner.runStep( step, testExecutionContext ) )
                  {
                     log.error( "Scenario " + testScenario + " failed at setup step " + step );
                     continue nextScenario;
                  }
               }

               for ( TestStep step : testScenario.getScenarioExecutionSteps() )
               {
                  testData = testDataSource.getData( new ExecutionStepPointer( testFeature.getName(), testScenario.getName(), step, iterationIndex, stepIndex++ ) );
                  log.debug( "Step test data is " + testData.toString() );
                  testExecutionContext.setTestData( testData );

                  if ( !stepRunner.runStep( step, testExecutionContext ) )
                  {
                     log.error( "Scenario " + testScenario + " failed at step " + step );
                     continue nextScenario;
                  }
               }
            }
            catch ( Throwable t )
            {

            }
            finally
            {
               for ( TestStep step : testFeature.getScenarioTearDownSteps() )
               {
                  testData = testDataSource.getData( new ExecutionStepPointer( testFeature.getName(), testScenario.getName(), step, iterationIndex, stepIndex++ ) );
                  log.debug( "Step test data is " + testData.toString() );
                  testExecutionContext.setTestData( testData );

                  try
                  {
                     if ( !stepRunner.runStep( step, testExecutionContext ) )
                     {
                        log.error( "Scenario " + testScenario + " failed at teardown step " + step );
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
            testData = testDataSource.getData( new ExecutionStepPointer( testFeature.getName(), null, step, iterationIndex, stepIndex++ ) );
            log.debug( "Step test data is " + testData.toString() );
            testExecutionContext.setTestData( testData );

            if ( !stepRunner.runStep( step, testExecutionContext ) )
            {
               log.error( "Feature setup teardown failed at step " + step );
               // return;
            }
         }
      }
      catch ( ClassNotFoundException cnfe )
      {
         log.error( "Plugin class load exception", cnfe );
      }
      catch ( Throwable t )
      {
         log.error( "Exception occured when trying to run test", t );
      }
   }
}
