package org.mvss.karta.framework.runtime;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.configuration.KartaBaseConfiguration;
import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.minions.KartaMinionConfiguration;
import org.mvss.karta.framework.minions.KartaMinionRegistry;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.RunCompleteEvent;
import org.mvss.karta.framework.runtime.event.RunStartEvent;
import org.mvss.karta.framework.runtime.interfaces.FeatureSourceParser;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.interfaces.TestEventListener;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;
import org.mvss.karta.framework.runtime.testcatalog.Test;
import org.mvss.karta.framework.runtime.testcatalog.TestCatalogManager;
import org.mvss.karta.framework.runtime.testcatalog.TestCategory;
import org.mvss.karta.framework.utils.ClassPathLoaderUtils;
import org.mvss.karta.framework.utils.DynamicClassLoader;
import org.mvss.karta.framework.utils.ParserUtils;
import org.mvss.karta.framework.utils.SSLUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class KartaRuntime implements AutoCloseable
{
   @Getter
   private Random                    random           = new Random();

   @Getter
   private KartaBaseConfiguration    kartaBaseConfiguration;

   @Getter
   private KartaRuntimeConfiguration kartaRuntimeConfiguration;

   @Getter
   private PnPRegistry               pnpRegistry;

   @Getter
   private Configurator              configurator;

   @Getter
   private TestCatalogManager        testCatalogManager;

   @Getter
   private EventProcessor            eventProcessor;

   @Getter
   private KartaMinionRegistry       minionRegistry;

   @Getter
   private KartaThreadFactory        kartaThreadFactory;

   private static ObjectMapper       yamlObjectMapper = ParserUtils.getYamlObjectMapper();

   public boolean initializeRuntime() throws JsonMappingException, JsonProcessingException, IOException, URISyntaxException, IllegalArgumentException, IllegalAccessException, NotBoundException
   {
      random = new Random();

      // if ( kartaConfiguration == null )
      // {
      kartaBaseConfiguration = yamlObjectMapper.readValue( ClassPathLoaderUtils.readAllText( Constants.KARTA_BASE_CONFIG_YAML ), KartaBaseConfiguration.class );
      // }

      // Configurator should be setup before plugin initialization

      // if ( configurator == null )
      // {
      configurator = new Configurator();
      // }

      // if ( pnPRegistry == null )
      // {
      pnpRegistry = new PnPRegistry();

      // TODO: Move plugin types to Karta Configuration
      pnpRegistry.addPluginType( FeatureSourceParser.class );
      pnpRegistry.addPluginType( StepRunner.class );
      pnpRegistry.addPluginType( TestDataSource.class );
      pnpRegistry.addPluginType( TestEventListener.class );
      // }

      pnpRegistry.addPluginConfiguration( kartaBaseConfiguration.getPluginConfigs() );

      // if ( runtimeConfiguration == null )
      // {
      kartaRuntimeConfiguration = yamlObjectMapper.readValue( ClassPathLoaderUtils.readAllText( Constants.KARTA_RUNTIME_CONFIGURATION_YAML ), KartaRuntimeConfiguration.class );
      // }

      String pluginsDirectory = kartaRuntimeConfiguration.getPluginsDirectory();

      if ( StringUtils.isNotEmpty( pluginsDirectory ) )
      {
         pnpRegistry.loadPlugins( configurator, new File( pluginsDirectory ) );
      }

      SSLUtils.setSslProperties( kartaRuntimeConfiguration.getSslProperties() );

      ArrayList<String> propertiesFileList = kartaRuntimeConfiguration.getPropertyFiles();
      if ( ( propertiesFileList != null ) && !propertiesFileList.isEmpty() )
      {
         String[] propertyFilesToLoad = new String[propertiesFileList.size()];
         propertiesFileList.toArray( propertyFilesToLoad );
         configurator.mergePropertiesFiles( propertyFilesToLoad );
      }

      pnpRegistry.enablePlugins( kartaRuntimeConfiguration.getEnabledPlugins() );

      if ( !pnpRegistry.initializePlugins( configurator.getPropertiesStore() ) )
      {
         return false;
      }

      eventProcessor = new EventProcessor();

      pnpRegistry.getEnabledPluginsOfType( TestEventListener.class ).forEach( ( plugin ) -> eventProcessor.addEventListener( (TestEventListener) plugin ) );

      configurator.loadProperties( eventProcessor );
      eventProcessor.start();

      minionRegistry = new KartaMinionRegistry();

      HashMap<String, KartaMinionConfiguration> nodeMap = kartaRuntimeConfiguration.getMinions();

      for ( String nodeName : nodeMap.keySet() )
      {
         minionRegistry.addMinion( nodeName, nodeMap.get( nodeName ) );
      }

      testCatalogManager = new TestCatalogManager();

      ArrayList<String> testCatalogFiles = kartaRuntimeConfiguration.getTestCatalogFiles();

      if ( ( testCatalogFiles != null ) && !testCatalogFiles.isEmpty() )
      {
         for ( String testCatalogFile : testCatalogFiles )
         {
            TestCategory testCategory = yamlObjectMapper.readValue( ClassPathLoaderUtils.readAllText( testCatalogFile ), TestCategory.class );
            testCatalogManager.mergeWithCatalog( testCategory );
         }
      }

      ArrayList<String> repoDirNames = kartaRuntimeConfiguration.getTestRepositorydirectories();

      if ( repoDirNames != null )
      {
         for ( String testRepositoryDirectoryName : repoDirNames )
         {
            File repositoryDirectory = new File( testRepositoryDirectoryName );

            if ( repositoryDirectory.exists() && repositoryDirectory.isDirectory() )
            {
               testCatalogManager.mergeRepositoryDirectoryIntoCatalog( repositoryDirectory );
            }
         }
      }

      kartaThreadFactory = new KartaThreadFactory();

      return true;
   }

   @Override
   public void close()
   {
      // TODO: Perform save actions and close threads

      if ( eventProcessor != null )
      {
         eventProcessor.close();
      }

      if ( pnpRegistry != null )
      {
         pnpRegistry.close();
      }
   }

   // Default constructor is reserved for default runtime instance use getDefaultInstance
   private KartaRuntime() throws JsonMappingException, JsonProcessingException, IOException, URISyntaxException
   {

   }

   private static KartaRuntime instance        = null;

   private static Object       _syncLockObject = new Object();

   public static KartaRuntime getInstance() throws Throwable
   {
      if ( instance == null )
      {
         synchronized ( _syncLockObject )
         {
            instance = new KartaRuntime();

            if ( !instance.initializeRuntime() )
            {
               instance = null;
            }
         }
      }

      return instance;
   }

   public static HashMap<String, Serializable> getMergedTestData( HashMap<String, Serializable> stepTestData, ArrayList<TestDataSource> testDataSources, ExecutionStepPointer executionStepPointer ) throws Throwable
   {
      HashMap<String, Serializable> mergedTestData = new HashMap<String, Serializable>();
      for ( TestDataSource tds : testDataSources )
      {
         HashMap<String, Serializable> testData = tds.getData( executionStepPointer );
         testData.forEach( ( key, value ) -> mergedTestData.put( key, value ) );
      }

      if ( stepTestData != null )
      {
         stepTestData.forEach( ( key, value ) -> mergedTestData.put( key, value ) );
      }
      return mergedTestData;
   }

   public boolean runFeatureFile( String runName, String featureSourceParserPlugin, String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String featureFileName, long numberOfIterations, int numberOfIterationsInParallel )
   {
      try
      {
         return runFeatureSource( runName, featureSourceParserPlugin, stepRunnerPlugin, testDataSourcePlugins, ClassPathLoaderUtils.readAllText( featureFileName ), numberOfIterations, numberOfIterationsInParallel );
      }
      catch ( Throwable t )
      {
         log.error( t );
         return false;
      }
   }

   public boolean runFeatureSource( String runName, String featureFileSourceString, long numberOfIterations, int numberOfIterationsInParallel )
   {
      return runFeatureSource( runName, kartaRuntimeConfiguration.getDefaultFeatureSourceParserPlugin(), kartaRuntimeConfiguration.getDefaultStepRunnerPlugin(), kartaRuntimeConfiguration
               .getDefaultTestDataSourcePlugins(), featureFileSourceString, numberOfIterations, numberOfIterationsInParallel );
   }

   public boolean runFeatureSource( String runName, String featureSourceParserPlugin, String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String featureFileSourceString, long numberOfIterations, int numberOfIterationsInParallel )
   {
      try
      {
         FeatureSourceParser featureParser = (FeatureSourceParser) pnpRegistry.getPlugin( featureSourceParserPlugin, FeatureSourceParser.class );

         if ( featureParser == null )
         {
            log.error( "Failed to get a feature source parser of type: " + kartaRuntimeConfiguration.getDefaultFeatureSourceParserPlugin() );
            return false;
         }
         TestFeature testFeature = featureParser.parseFeatureSource( featureFileSourceString );

         return runFeature( stepRunnerPlugin, testDataSourcePlugins, runName, testFeature, numberOfIterations, numberOfIterationsInParallel );
      }
      catch ( Throwable t )
      {
         log.error( t );
         return false;
      }
   }

   public boolean runFeature( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, TestFeature feature, long numberOfIterations, int numberOfIterationsInParallel )
   {
      StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( stepRunnerPlugin, StepRunner.class );

      if ( stepRunner == null )
      {
         log.error( "Failed to get a step runner of type: " + kartaRuntimeConfiguration.getDefaultStepRunnerPlugin() );
         return false;
      }

      ArrayList<TestDataSource> testDataSources = new ArrayList<TestDataSource>();

      for ( String testDataSourcePlugin : testDataSourcePlugins )
      {
         TestDataSource testDataSource = (TestDataSource) pnpRegistry.getPlugin( testDataSourcePlugin, TestDataSource.class );

         if ( testDataSource == null )
         {
            log.error( "Failed to get a test data source of type: " + testDataSourcePlugin );
            return false;
         }

         testDataSources.add( testDataSource );
      }

      try
      {
         FeatureRunner featureRunner = FeatureRunner.builder().kartaRuntime( this ).stepRunner( stepRunner ).testDataSources( testDataSources ).build();
         // configurator.loadProperties( featureRunner );
         return featureRunner.run( runName, feature, numberOfIterations, numberOfIterationsInParallel );
      }
      catch ( Throwable t )
      {
         log.error( t );
      }
      return false;
   }

   // TODO: Move running based on run target here

   public boolean runTestTarget( String runName, RunTarget runTarget )
   {
      return runTestTarget( runName, kartaRuntimeConfiguration.getDefaultStepRunnerPlugin(), kartaRuntimeConfiguration.getDefaultStepRunnerPlugin(), kartaRuntimeConfiguration.getDefaultTestDataSourcePlugins(), runTarget );
   }

   public ArrayList<TestDataSource> getTestDataSourcePlugins( HashSet<String> testDataSourcePlugins )
   {
      ArrayList<TestDataSource> testDataSources = new ArrayList<TestDataSource>();
      for ( String testDataSourcePlugin : testDataSourcePlugins )
      {
         TestDataSource testDataSource = (TestDataSource) pnpRegistry.getPlugin( testDataSourcePlugin, TestDataSource.class );

         if ( testDataSource == null )
         {
            log.error( "Failed to get a test data source of type: " + testDataSourcePlugin );
            return null;
         }
         testDataSources.add( testDataSource );
      }
      return testDataSources;
   }

   public boolean runTestTarget( String runName, String featureSourceParserPlugin, String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, RunTarget runTarget )
   {
      if ( StringUtils.isNotBlank( runTarget.getFeatureFile() ) )
      {
         eventProcessor.raiseEvent( new RunStartEvent( runName ) );
         boolean result = runFeatureFile( runName, featureSourceParserPlugin, stepRunnerPlugin, testDataSourcePlugins, runTarget.getFeatureFile(), runTarget.getNumberOfIterations(), runTarget.getNumberOfThreads() );
         eventProcessor.raiseEvent( new RunCompleteEvent( runName ) );
         return result;
      }
      else if ( StringUtils.isNotBlank( runTarget.getJavaTest() ) )
      {
         ArrayList<TestDataSource> testDataSources = getTestDataSourcePlugins( testDataSourcePlugins );
         if ( testDataSources == null )
         {
            eventProcessor.raiseEvent( new RunCompleteEvent( runName ) );
            return false;
         }

         JavaFeatureRunner testRunner = JavaFeatureRunner.builder().kartaRuntime( this ).testDataSources( testDataSources ).build();
         eventProcessor.raiseEvent( new RunStartEvent( runName ) );
         boolean result = testRunner.run( runName, runTarget.getJavaTest(), runTarget.getJavaTestJarFile(), runTarget.getNumberOfIterations(), runTarget.getNumberOfThreads() );
         eventProcessor.raiseEvent( new RunCompleteEvent( runName ) );
         return result;
      }
      else if ( ( runTarget.getTags() != null && !runTarget.getTags().isEmpty() ) )
      {
         return runTestsWithTags( runName, runTarget.getTags() );
      }
      else
      {
         return false;
      }
   }

   public boolean runTestsWithTags( String runName, HashSet<String> tags )
   {
      try
      {
         eventProcessor.raiseEvent( new RunStartEvent( runName ) );

         ArrayList<Test> tests = testCatalogManager.filterTestsByTag( tags );

         for ( Test test : tests )
         {
            ArrayList<TestDataSource> testDataSources = new ArrayList<TestDataSource>();

            switch ( test.getTestType() )
            {
               case FEATURE:
                  FeatureSourceParser featureParser = (FeatureSourceParser) pnpRegistry.getPlugin( test.getFeatureSourceParserPlugin(), FeatureSourceParser.class );

                  if ( featureParser == null )
                  {
                     log.error( "Failed to get a feature source parser of type: " + test.getFeatureSourceParserPlugin() );
                     eventProcessor.raiseEvent( new RunCompleteEvent( runName ) );
                     return false;
                  }
                  // TODO: Handle io errors
                  TestFeature testFeature = featureParser.parseFeatureSource( IOUtils.toString( DynamicClassLoader.getClassPathResourceInJarAsStream( test.getSourceArchive(), test.getFeatureFileName() ), Charset.defaultCharset() ) );

                  StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( test.getStepRunnerPlugin(), StepRunner.class );

                  if ( stepRunner == null )
                  {
                     log.error( "Failed to get a step runner of type: " + test.getStepRunnerPlugin() );
                     eventProcessor.raiseEvent( new RunCompleteEvent( runName ) );
                     return false;
                  }

                  for ( String testDataSourcePlugin : test.getTestDataSourcePlugins() )
                  {
                     TestDataSource testDataSource = (TestDataSource) pnpRegistry.getPlugin( testDataSourcePlugin, TestDataSource.class );

                     if ( testDataSource == null )
                     {
                        log.error( "Failed to get a test data source of type: " + testDataSourcePlugin );
                        eventProcessor.raiseEvent( new RunCompleteEvent( runName ) );
                        return false;
                     }

                     testDataSources.add( testDataSource );
                  }

                  FeatureRunner featureRunner = FeatureRunner.builder().kartaRuntime( this ).stepRunner( stepRunner ).testDataSources( testDataSources ).build();
                  featureRunner.run( runName, testFeature, test.getNumberOfIterations(), test.getNumberOfThreads() );
                  break;

               case JAVA_TEST:
                  for ( String testDataSourcePlugin : test.getTestDataSourcePlugins() )
                  {
                     TestDataSource testDataSource = (TestDataSource) pnpRegistry.getPlugin( testDataSourcePlugin, TestDataSource.class );

                     if ( testDataSource == null )
                     {
                        log.error( "Failed to get a test data source of type: " + testDataSourcePlugin );
                        eventProcessor.raiseEvent( new RunCompleteEvent( runName ) );
                        return false;
                     }

                     testDataSources.add( testDataSource );
                  }
                  JavaFeatureRunner testRunner = JavaFeatureRunner.builder().kartaRuntime( this ).testDataSources( testDataSources ).build();
                  testRunner.run( runName, test.getJavaTestClass(), test.getSourceArchive(), test.getNumberOfIterations(), test.getNumberOfThreads() );
                  break;
            }
         }

         eventProcessor.raiseEvent( new RunCompleteEvent( runName ) );
      }
      catch ( Throwable t )
      {
         log.error( t );
         return false;
      }
      return true;
   }

   public StepResult runStep( String stepRunnerPlugin, TestStep step, TestExecutionContext context ) throws TestFailureException
   {
      StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( stepRunnerPlugin, StepRunner.class );
      if ( stepRunner == null )
      {
         return new StepResult( false, TestIncident.builder().message( "Step runner plugin not found: " + stepRunnerPlugin ).build(), null );
      }
      return stepRunner.runStep( step, context );
   }

   public boolean runTestScenario( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, TestFeature feature, int iterationIndex, TestScenario testScenario, int scenarioIterationNumber )
   {
      StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( stepRunnerPlugin, StepRunner.class );
      ArrayList<TestDataSource> testDataSources = getTestDataSourcePlugins( testDataSourcePlugins );
      if ( ( stepRunner == null ) || ( testDataSources == null ) )
      {
         log.error( "Plugin(s) not found: " + stepRunnerPlugin + testDataSourcePlugins );
         return false;
      }

      return ScenarioRunner.builder().kartaRuntime( this ).stepRunner( stepRunner ).testDataSources( testDataSources ).runName( runName ).feature( feature ).iterationIndex( iterationIndex ).testScenario( testScenario )
               .scenarioIterationNumber( scenarioIterationNumber ).build().run();
   }

   public StepResult runChaosAction( String stepRunnerPlugin, ChaosAction chaosAction, TestExecutionContext context ) throws TestFailureException
   {
      StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( stepRunnerPlugin, StepRunner.class );
      if ( stepRunner == null )
      {
         return new StepResult( false, TestIncident.builder().message( "Step runner plugin not found: " + stepRunnerPlugin ).build(), null );
      }
      return stepRunner.performChaosAction( chaosAction, context );
   }

   public StepResult runStepOnNode( String nodeName, String stepRunnerPlugin, TestStep step, TestExecutionContext context ) throws RemoteException
   {
      return minionRegistry.getMinion( nodeName ).runStep( stepRunnerPlugin, step, context );
   }

   public StepResult runChaosActionNode( String nodeName, String stepRunnerPlugin, ChaosAction chaosAction, TestExecutionContext context ) throws RemoteException
   {
      return minionRegistry.getMinion( nodeName ).performChaosAction( stepRunnerPlugin, chaosAction, context );
   }
}
