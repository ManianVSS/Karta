package org.mvss.karta.runner.core;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.FeatureSourceParser;
import org.mvss.karta.framework.runtime.StepRunner;
import org.mvss.karta.framework.runtime.TestDataSource;
import org.mvss.karta.framework.runtime.TestExecutionContext;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;
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
   public static final String                                RUN_PROPERTIES_FILE       = "Run.properties";

   private static final ExtensionLoader<FeatureSourceParser> featureParserClassLoader  = new ExtensionLoader<FeatureSourceParser>();
   private String                                            featureParserClassName;
   private String                                            featureParserJarFile;
   private HashMap<String, Serializable>                     featureParserProperties;

   private static final ExtensionLoader<StepRunner>          stepRunnerClassLoader     = new ExtensionLoader<StepRunner>();
   private String                                            stepRunnerClassName;
   private String                                            stepRunnerJarFile;
   private HashMap<String, Serializable>                     stepRunnerProperties;

   private static final ExtensionLoader<TestDataSource>      testDataSourceClassLoader = new ExtensionLoader<TestDataSource>();
   private String                                            testDataSourceClassName;
   private String                                            testDataSourceJarFile;
   private HashMap<String, Serializable>                     testDataSourceProperties;

   private String                                            featureFile;

   @SuppressWarnings( "unchecked" )
   @Override
   public void run()
   {
      try
      {
         Class<? extends FeatureSourceParser> featureParserClass = StringUtils.isNotBlank( featureParserJarFile ) ? featureParserClassLoader.LoadClass( new File( featureParserJarFile ), featureParserClassName )
                  : (Class<? extends FeatureSourceParser>) Class.forName( featureParserClassName );
         FeatureSourceParser featureParser = featureParserClass.newInstance();
         featureParser.initFeatureParser( featureParserProperties );

         Class<? extends TestDataSource> testDataSourceClass = StringUtils.isNotBlank( testDataSourceJarFile ) ? testDataSourceClassLoader.LoadClass( new File( testDataSourceJarFile ), testDataSourceClassName )
                  : (Class<? extends TestDataSource>) Class.forName( testDataSourceClassName );
         TestDataSource testDataSource = testDataSourceClass.newInstance();
         testDataSource.initDataSource( testDataSourceProperties );

         Class<? extends StepRunner> stepRunnerClass = StringUtils.isNotBlank( stepRunnerJarFile ) ? stepRunnerClassLoader.LoadClass( new File( stepRunnerJarFile ), stepRunnerClassName ) : (Class<? extends StepRunner>) Class.forName( stepRunnerClassName );
         StepRunner stepRunner = stepRunnerClass.newInstance();
         stepRunner.initStepRepository( stepRunnerProperties );

         TestFeature testFeature = featureParser.parseFeatureSource( FileUtils.readFileToString( new File( featureFile ), Charset.defaultCharset() ) );

         HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
         HashMap<String, Serializable> variables = new HashMap<String, Serializable>();

         TestExecutionContext testExecutionContext = new TestExecutionContext( stepRunnerProperties, testData, variables );

         long iterationIndex = -1;
         long stepIndex = 0;

         for ( TestStep step : testFeature.getTestSetupSteps() )
         {
            testData = testDataSource.getData( new ExecutionStepPointer( testFeature.getName(), null, step.getIdentifier(), iterationIndex, stepIndex++ ) );
            log.debug( "Step test data is " + testData.toString() );
            testExecutionContext.setTestData( testData );

            if ( !stepRunner.runStep( step, testExecutionContext ) )
            {
               log.error( "Feature setup failed at step " + step );
               return;
            }
         }

         iterationIndex = 0;

         nextScenario: for ( TestScenario testScenario : testFeature.getTestScenarios() )
         {
            log.debug( "Running Scenario: " + testScenario );

            for ( TestStep step : testFeature.getScenarioSetupSteps() )
            {
               testData = testDataSource.getData( new ExecutionStepPointer( testFeature.getName(), testScenario.getName(), step.getIdentifier(), iterationIndex, stepIndex++ ) );
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
               testData = testDataSource.getData( new ExecutionStepPointer( testFeature.getName(), testScenario.getName(), step.getIdentifier(), iterationIndex, stepIndex++ ) );
               log.debug( "Step test data is " + testData.toString() );
               testExecutionContext.setTestData( testData );

               if ( !stepRunner.runStep( step, testExecutionContext ) )
               {
                  log.error( "Scenario " + testScenario + " failed at step " + step );
                  continue nextScenario;
               }
            }

            for ( TestStep step : testFeature.getScenarioTearDownSteps() )
            {
               testData = testDataSource.getData( new ExecutionStepPointer( testFeature.getName(), testScenario.getName(), step.getIdentifier(), iterationIndex, stepIndex++ ) );
               log.debug( "Step test data is " + testData.toString() );
               testExecutionContext.setTestData( testData );

               if ( !stepRunner.runStep( step, testExecutionContext ) )
               {
                  log.error( "Scenario " + testScenario + " failed at teardown step " + step );
                  // break;
               }
            }
         }

         for ( TestStep step : testFeature.getTestTearDownSteps() )
         {
            testData = testDataSource.getData( new ExecutionStepPointer( testFeature.getName(), null, step.getIdentifier(), iterationIndex, stepIndex++ ) );
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
         log.error( "class " + stepRunnerClassName + " could not be loaded" );
      }
      catch ( Throwable t )
      {
         log.error( "Exception occured when trying to run test", t );
      }
   }
}
