package org.mvss.karta.framework.runtime;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.configuration.KartaBaseConfiguration;
import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StandardScenarioResults;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.minions.KartaMinion;
import org.mvss.karta.framework.minions.KartaMinionConfiguration;
import org.mvss.karta.framework.minions.KartaMinionRegistry;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.RunCompleteEvent;
import org.mvss.karta.framework.runtime.event.RunStartEvent;
import org.mvss.karta.framework.runtime.interfaces.FeatureSourceParser;
import org.mvss.karta.framework.runtime.interfaces.Plugin;
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
import org.quartz.SchedulerException;

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
   private KartaMinionRegistry       nodeRegistry;

   private static ObjectMapper       yamlObjectMapper = ParserUtils.getYamlObjectMapper();

   @Getter
   private HashSet<Object>           beans;

   @Getter
   private ExecutorServiceManager    executorServiceManager;

   @SuppressWarnings( "unchecked" )
   public boolean initializeRuntime() throws JsonMappingException, JsonProcessingException, IOException, URISyntaxException, IllegalArgumentException, IllegalAccessException, NotBoundException, ClassNotFoundException
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

      for ( String pluginType : kartaBaseConfiguration.getPluginTypes() )
      {
         pnpRegistry.addPluginType( (Class<? extends Plugin>) Class.forName( pluginType ) );
      }

      pnpRegistry.addPluginConfiguration( kartaBaseConfiguration.getPluginConfigs() );

      // if ( runtimeConfiguration == null )
      // {
      kartaRuntimeConfiguration = yamlObjectMapper.readValue( ClassPathLoaderUtils.readAllText( Constants.KARTA_RUNTIME_CONFIGURATION_YAML ), KartaRuntimeConfiguration.class );
      kartaRuntimeConfiguration.expandSystemAndEnvProperties();
      // }

      ArrayList<String> pluginDirectories = kartaRuntimeConfiguration.getPluginsDirectories();

      if ( pluginDirectories != null )
      {
         for ( String pluginsDirectory : pluginDirectories )
         {
            if ( StringUtils.isNotEmpty( pluginsDirectory ) )
            {
               pnpRegistry.loadPlugins( configurator, new File( pluginsDirectory ) );
            }
         }
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

      eventProcessor = new EventProcessor();

      pnpRegistry.getEnabledPluginsOfType( TestEventListener.class ).forEach( ( plugin ) -> eventProcessor.addEventListener( (TestEventListener) plugin ) );

      configurator.loadProperties( eventProcessor );
      eventProcessor.start();

      // TODO: Add task pulling worker minions to support minions as clients rather than open server sockets
      nodeRegistry = new KartaMinionRegistry();

      HashMap<String, KartaMinionConfiguration> nodeMap = kartaRuntimeConfiguration.getNodes();

      for ( String nodeName : nodeMap.keySet() )
      {
         nodeRegistry.addNode( nodeName, nodeMap.get( nodeName ) );
      }

      testCatalogManager = new TestCatalogManager();

      String catalogFileText = ClassPathLoaderUtils.readAllText( Constants.TEST_CATALOG_FILE_NAME );
      TestCategory testCategory = ( catalogFileText == null ) ? new TestCategory() : yamlObjectMapper.readValue( catalogFileText, TestCategory.class );
      testCatalogManager.mergeWithCatalog( testCategory );

      ArrayList<String> testCatalogFragmentFiles = kartaRuntimeConfiguration.getTestCatalogFragmentFiles();

      if ( testCatalogFragmentFiles != null )
      {
         for ( String testCatalogFragmentFile : kartaRuntimeConfiguration.getTestCatalogFragmentFiles() )
         {
            catalogFileText = FileUtils.readFileToString( new File( testCatalogFragmentFile ), Charset.defaultCharset() );
            testCategory = ( catalogFileText == null ) ? new TestCategory() : yamlObjectMapper.readValue( catalogFileText, TestCategory.class );
            testCatalogManager.mergeWithCatalog( testCategory );
         }
      }

      testCatalogManager.mergeRepositoryDirectoryIntoCatalog( new File( Constants.DOT ) );

      executorServiceManager = new ExecutorServiceManager();
      executorServiceManager.getOrAddExecutorServiceForGroup( Constants.__TESTS__, kartaRuntimeConfiguration.getTestThreadCount() );

      beans = new HashSet<Object>();
      beans.add( configurator );
      beans.add( testCatalogManager );
      beans.add( eventProcessor );
      beans.add( nodeRegistry );
      beans.add( executorServiceManager );

      if ( !pnpRegistry.initializePlugins( this ) )
      {
         return false;
      }

      return true;
   }

   @Override
   public void close()
   {
      if ( executorServiceManager != null )
      {
         executorServiceManager.close();
      }

      try
      {
         QuartzJobScheduler.shutdown();
      }
      catch ( SchedulerException e )
      {
         log.error( e );
      }

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

   public static HashMap<String, Serializable> getMergedTestData( String runName, HashMap<String, ArrayList<Serializable>> stepTestData, ArrayList<TestDataSource> testDataSources, ExecutionStepPointer executionStepPointer ) throws Throwable
   {
      HashMap<String, Serializable> mergedTestData = new HashMap<String, Serializable>();
      for ( TestDataSource tds : testDataSources )
      {
         HashMap<String, Serializable> testData = tds.getData( executionStepPointer );
         testData.forEach( ( key, value ) -> mergedTestData.put( key, value ) );
      }

      if ( stepTestData != null )
      {
         // stepTestData.forEach( ( key, value ) -> mergedTestData.put( key, value ) );

         int iterationIndex = ( executionStepPointer != null ) ? executionStepPointer.getIterationIndex() : 0;
         if ( iterationIndex <= 0 )
         {
            iterationIndex = 0;
         }
         for ( String dataKey : stepTestData.keySet() )
         {
            ArrayList<Serializable> possibleValues = stepTestData.get( dataKey );
            if ( ( possibleValues != null ) && !possibleValues.isEmpty() )
            {
               int valueIndex = iterationIndex % possibleValues.size();
               mergedTestData.put( dataKey, possibleValues.get( valueIndex ) );
            }
         }
      }

      return mergedTestData;
   }

   public void loadRuntimeObjects( Object object ) throws IllegalArgumentException, IllegalAccessException
   {
      Configurator.loadBeans( object, beans );
   }

   public boolean runFeatureFile( String runName, String featureSourceParserPlugin, String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String featureFileName, boolean chanceBasedScenarioExecution, boolean exclusiveScenarioPerIteration,
                                  long numberOfIterations, int numberOfIterationsInParallel )
   {
      try
      {
         String featureSource = ClassPathLoaderUtils.readAllText( featureFileName );

         if ( StringUtils.isEmpty( featureSource ) )
         {
            log.error( "Feature file invalid: " + featureFileName );
            return false;
         }
         return runFeatureSource( runName, featureSourceParserPlugin, stepRunnerPlugin, testDataSourcePlugins, featureSource, chanceBasedScenarioExecution, exclusiveScenarioPerIteration, numberOfIterations, numberOfIterationsInParallel );
      }
      catch ( Throwable t )
      {
         log.error( t );
         return false;
      }
   }

   public boolean runFeatureSource( String runName, String featureFileSourceString, boolean chanceBasedScenarioExecution, boolean exclusiveScenarioPerIteration, long numberOfIterations, int numberOfIterationsInParallel )
   {
      return runFeatureSource( runName, kartaRuntimeConfiguration.getDefaultFeatureSourceParserPlugin(), kartaRuntimeConfiguration.getDefaultStepRunnerPlugin(), kartaRuntimeConfiguration
               .getDefaultTestDataSourcePlugins(), featureFileSourceString, chanceBasedScenarioExecution, exclusiveScenarioPerIteration, numberOfIterations, numberOfIterationsInParallel );
   }

   public boolean runFeatureSource( String runName, String featureSourceParserPlugin, String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String featureFileSourceString, boolean chanceBasedScenarioExecution,
                                    boolean exclusiveScenarioPerIteration, long numberOfIterations, int numberOfIterationsInParallel )
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

         return runFeature( stepRunnerPlugin, testDataSourcePlugins, runName, testFeature, chanceBasedScenarioExecution, exclusiveScenarioPerIteration, numberOfIterations, numberOfIterationsInParallel );
      }
      catch ( Throwable t )
      {
         log.error( t );
         return false;
      }
   }

   public boolean runFeature( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, TestFeature feature, boolean chanceBasedScenarioExecution, boolean exclusiveScenarioPerIteration, long numberOfIterations,
                              int numberOfIterationsInParallel )
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
         FeatureRunner featureRunner = FeatureRunner.builder().kartaRuntime( this ).stepRunner( stepRunner ).testDataSources( testDataSources ).chanceBasedScenarioExecution( chanceBasedScenarioExecution )
                  .exclusiveScenarioPerIteration( exclusiveScenarioPerIteration ).runName( runName ).testFeature( feature ).numberOfIterations( numberOfIterations ).numberOfIterationsInParallel( numberOfIterationsInParallel ).build();
         return featureRunner.call();
      }
      catch ( Throwable t )
      {
         log.error( t );
         return false;
      }
   }

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
      try
      {
         if ( StringUtils.isNotBlank( runTarget.getFeatureFile() ) )
         {
            eventProcessor.raiseEvent( new RunStartEvent( runName ) );
            boolean result = runFeatureFile( runName, featureSourceParserPlugin, stepRunnerPlugin, testDataSourcePlugins, runTarget.getFeatureFile(), runTarget.getChanceBasedScenarioExecution(), runTarget.getExclusiveScenarioPerIteration(), runTarget
                     .getNumberOfIterations(), runTarget.getNumberOfThreads() );
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

            JavaFeatureRunner testRunner = JavaFeatureRunner.builder().kartaRuntime( this ).testDataSources( testDataSources ).runName( runName ).javaTest( runTarget.getJavaTest() ).javaTestJarFile( runTarget.getJavaTestJarFile() )
                     .numberOfIterations( runTarget.getNumberOfIterations() ).numberOfIterationsInParallel( runTarget.getNumberOfThreads() ).chanceBasedScenarioExecution( runTarget.getChanceBasedScenarioExecution() )
                     .exclusiveScenarioPerIteration( runTarget.getExclusiveScenarioPerIteration() ).build();
            eventProcessor.raiseEvent( new RunStartEvent( runName ) );
            boolean result = testRunner.call();
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
      catch ( Throwable t )
      {
         log.error( t );
         return false;
      }
   }

   public boolean runTestsWithTags( String runName, HashSet<String> tags ) throws Throwable
   {
      eventProcessor.raiseEvent( new RunStartEvent( runName ) );
      ArrayList<Test> tests = testCatalogManager.filterTestsByTag( tags );
      Collections.sort( tests );
      boolean result = runTest( runName, tests );
      eventProcessor.raiseEvent( new RunCompleteEvent( runName ) );
      return result;
   }

   public boolean runTest( String runName, Collection<Test> tests ) throws Throwable
   {
      ArrayList<Future<Boolean>> futures = new ArrayList<Future<Boolean>>();

      boolean successful = true;

      boolean enabledMinions = kartaRuntimeConfiguration.isMinionsEnabled() && !nodeRegistry.getMinions().isEmpty();

      for ( Test test : tests )
      {
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

               ArrayList<TestDataSource> featureTestDataSources = new ArrayList<TestDataSource>();

               for ( String testDataSourcePlugin : test.getTestDataSourcePlugins() )
               {
                  TestDataSource testDataSource = (TestDataSource) pnpRegistry.getPlugin( testDataSourcePlugin, TestDataSource.class );

                  if ( testDataSource == null )
                  {
                     log.error( "Failed to get a test data source of type: " + testDataSourcePlugin );
                     eventProcessor.raiseEvent( new RunCompleteEvent( runName ) );
                     return false;
                  }

                  featureTestDataSources.add( testDataSource );
               }

               ExecutorService testExecutorService = executorServiceManager.getExecutorServiceForGroup( test.getThreadGroup() );

               if ( enabledMinions )
               {
                  KartaMinion minion = nodeRegistry.getNextMinion();

                  if ( minion != null )
                  {
                     futures.add( testExecutorService.submit( () -> {
                        return minion.runFeature( test.getStepRunnerPlugin(), test.getTestDataSourcePlugins(), runName, testFeature, test.getChanceBasedScenarioExecution(), test.getExclusiveScenarioPerIteration(), test.getNumberOfIterations(), test
                                 .getNumberOfThreads() );
                     } ) );
                  }
               }

               FeatureRunner featureRunner = FeatureRunner.builder().kartaRuntime( this ).stepRunner( stepRunner ).testDataSources( featureTestDataSources ).chanceBasedScenarioExecution( test.getChanceBasedScenarioExecution() )
                        .exclusiveScenarioPerIteration( test.getExclusiveScenarioPerIteration() ).runName( runName ).testFeature( testFeature ).numberOfIterations( test.getNumberOfIterations() ).numberOfIterationsInParallel( test.getNumberOfThreads() )
                        .build();

               futures.add( testExecutorService.submit( featureRunner ) );
               break;

            case JAVA_TEST:
               ArrayList<TestDataSource> javaTestDataSources = new ArrayList<TestDataSource>();
               for ( String testDataSourcePlugin : test.getTestDataSourcePlugins() )
               {
                  TestDataSource testDataSource = (TestDataSource) pnpRegistry.getPlugin( testDataSourcePlugin, TestDataSource.class );

                  if ( testDataSource == null )
                  {
                     log.error( "Failed to get a test data source of type: " + testDataSourcePlugin );
                     eventProcessor.raiseEvent( new RunCompleteEvent( runName ) );
                     return false;
                  }

                  javaTestDataSources.add( testDataSource );
               }
               JavaFeatureRunner testRunner = JavaFeatureRunner.builder().kartaRuntime( this ).testDataSources( javaTestDataSources ).runName( runName ).javaTest( test.getJavaTestClass() ).javaTestJarFile( test.getSourceArchive() )
                        .numberOfIterations( test.getNumberOfIterations() ).numberOfIterationsInParallel( test.getNumberOfThreads() ).chanceBasedScenarioExecution( test.getChanceBasedScenarioExecution() )
                        .exclusiveScenarioPerIteration( test.getExclusiveScenarioPerIteration() ).build();
               testExecutorService = executorServiceManager.getExecutorServiceForGroup( test.getThreadGroup() );
               futures.add( testExecutorService.submit( testRunner ) );
               break;
         }
      }

      for ( Future<Boolean> future : futures )
      {
         successful = successful && future.get();
      }

      return successful;
   }

   public StepResult runStep( String stepRunnerPlugin, TestStep testStep, TestExecutionContext testExecutionContext ) throws TestFailureException
   {
      StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( stepRunnerPlugin, StepRunner.class );
      if ( stepRunner == null )
      {
         return StandardStepResults.error( TestIncident.builder().message( "Step runner plugin not found: " + stepRunnerPlugin ).build() );
      }
      return stepRunner.runStep( testStep, testExecutionContext );
   }

   public ScenarioResult runTestScenario( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, String featureName, int iterationIndex, ArrayList<TestStep> scenarioSetupSteps, TestScenario testScenario,
                                          ArrayList<TestStep> scenarioTearDownSteps, int scenarioIterationNumber )
   {
      StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( stepRunnerPlugin, StepRunner.class );
      ArrayList<TestDataSource> testDataSources = getTestDataSourcePlugins( testDataSourcePlugins );
      if ( ( stepRunner == null ) || ( testDataSources == null ) )
      {
         log.error( "Plugin(s) not found: " + stepRunnerPlugin + testDataSourcePlugins );
         return StandardScenarioResults.error( TestIncident.builder().message( "Plugin(s) not found: " + stepRunnerPlugin + testDataSourcePlugins ).build() );
      }

      return runTestScenario( stepRunner, testDataSources, runName, featureName, iterationIndex, scenarioSetupSteps, testScenario, scenarioTearDownSteps, scenarioIterationNumber );
   }

   public ScenarioResult runTestScenario( StepRunner stepRunner, ArrayList<TestDataSource> testDataSources, String runName, String featureName, int iterationIndex, ArrayList<TestStep> scenarioSetupSteps, TestScenario testScenario,
                                          ArrayList<TestStep> scenarioTearDownSteps, int scenarioIterationNumber )
   {
      ScenarioRunner scenarioRunner = ScenarioRunner.builder().kartaRuntime( this ).stepRunner( stepRunner ).testDataSources( testDataSources ).runName( runName ).featureName( featureName ).iterationIndex( iterationIndex )
               .scenarioSetupSteps( scenarioSetupSteps ).testScenario( testScenario ).scenarioTearDownSteps( scenarioTearDownSteps ).scenarioIterationNumber( scenarioIterationNumber ).build();
      scenarioRunner.run();
      return scenarioRunner.getResult();
   }

   public StepResult runChaosAction( String stepRunnerPlugin, ChaosAction chaosAction, TestExecutionContext context ) throws TestFailureException
   {
      StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( stepRunnerPlugin, StepRunner.class );
      if ( stepRunner == null )
      {
         return StandardStepResults.error( TestIncident.builder().message( "Step runner plugin not found: " + stepRunnerPlugin ).build() );
      }
      return stepRunner.performChaosAction( chaosAction, context );
   }

   public StepResult runStepOnNode( String nodeName, String stepRunnerPlugin, TestStep step, TestExecutionContext context ) throws RemoteException
   {
      return nodeRegistry.getNode( nodeName ).runStep( stepRunnerPlugin, step, context );
   }

   public StepResult runChaosActionNode( String nodeName, String stepRunnerPlugin, ChaosAction chaosAction, TestExecutionContext context ) throws RemoteException
   {
      return nodeRegistry.getNode( nodeName ).performChaosAction( stepRunnerPlugin, chaosAction, context );
   }
}
