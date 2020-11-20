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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.configuration.KartaConfiguration;
import org.mvss.karta.configuration.PluginConfig;
import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StandardFeatureResults;
import org.mvss.karta.framework.core.StandardScenarioResults;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.core.TestJob;
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
import org.mvss.karta.framework.runtime.interfaces.TestLifeCycleHook;
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
   private Random                 random           = new Random();

   @Getter
   private KartaConfiguration     kartaConfiguration;

   @Getter
   private PnPRegistry            pnpRegistry;

   @Getter
   private Configurator           configurator;

   @Getter
   private TestCatalogManager     testCatalogManager;

   @Getter
   private EventProcessor         eventProcessor;

   @Getter
   private KartaMinionRegistry    nodeRegistry;

   private static ObjectMapper    yamlObjectMapper = ParserUtils.getYamlObjectMapper();

   @Getter
   private BeanRegistry           beanRegistry;

   @Getter
   private ExecutorServiceManager executorServiceManager;

   public static boolean          initializeNodes  = true;

   public boolean initializeRuntime() throws JsonMappingException, JsonProcessingException, IOException, URISyntaxException, IllegalArgumentException, IllegalAccessException, NotBoundException, ClassNotFoundException
   {
      /*---------------------------------------------------------------------------------------------------------------------*/
      // Initialize karta configuration
      /*---------------------------------------------------------------------------------------------------------------------*/
      kartaConfiguration = yamlObjectMapper.readValue( ClassPathLoaderUtils.readAllText( Constants.KARTA_CONFIGURATION_YAML ), KartaConfiguration.class );
      kartaConfiguration.expandSystemAndEnvProperties();

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Intialize random
      /*---------------------------------------------------------------------------------------------------------------------*/
      random = new Random();

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Initialize SSL properties
      /*---------------------------------------------------------------------------------------------------------------------*/
      SSLUtils.setSslProperties( kartaConfiguration.getSslProperties() );

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Configurator should be setup before plug-in initialization
      /*---------------------------------------------------------------------------------------------------------------------*/
      configurator = new Configurator();
      ArrayList<String> propertiesFileList = kartaConfiguration.getPropertyFiles();
      if ( ( propertiesFileList != null ) && !propertiesFileList.isEmpty() )
      {
         String[] propertyFilesToLoad = new String[propertiesFileList.size()];
         propertiesFileList.toArray( propertyFilesToLoad );
         configurator.mergePropertiesFiles( propertyFilesToLoad );
      }

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Load and enable plug-ins
      /*---------------------------------------------------------------------------------------------------------------------*/
      pnpRegistry = new PnPRegistry();
      // kartaBaseConfiguration = yamlObjectMapper.readValue( ClassPathLoaderUtils.readAllText( Constants.KARTA_BASE_CONFIG_YAML ), KartaBaseConfiguration.class );
      ArrayList<PluginConfig> basePluginConfigs = PnPRegistry.readPluginsConfig( Constants.KARTA_BASE_PLUGIN_CONFIG_YAML );
      pnpRegistry.addPluginConfiguration( basePluginConfigs );// kartaBaseConfiguration.getPluginConfigs() );

      ArrayList<String> pluginDirectories = kartaConfiguration.getPluginsDirectories();
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

      pnpRegistry.enablePlugins( kartaConfiguration.getEnabledPlugins() );

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Initialize event processor
      /*---------------------------------------------------------------------------------------------------------------------*/
      eventProcessor = new EventProcessor();
      configurator.loadProperties( eventProcessor );
      pnpRegistry.getEnabledPluginsOfType( TestEventListener.class ).forEach( ( plugin ) -> eventProcessor.addEventListener( (TestEventListener) plugin ) );
      pnpRegistry.getEnabledPluginsOfType( TestLifeCycleHook.class ).forEach( ( plugin ) -> eventProcessor.addLifeCycleHook( (TestLifeCycleHook) plugin ) );

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Initialize node registry
      /*---------------------------------------------------------------------------------------------------------------------*/
      // TODO: Add task pulling worker minions to support minions as clients rather than open server sockets
      nodeRegistry = new KartaMinionRegistry();

      if ( initializeNodes )
      {
         addNodes();
      }

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Initialize Test catalog manager and load test catalog
      /*---------------------------------------------------------------------------------------------------------------------*/
      testCatalogManager = new TestCatalogManager();
      String catalogFileText = ClassPathLoaderUtils.readAllText( Constants.TEST_CATALOG_FILE_NAME );
      TestCategory testCategory = ( catalogFileText == null ) ? new TestCategory() : yamlObjectMapper.readValue( catalogFileText, TestCategory.class );
      testCatalogManager.mergeWithCatalog( testCategory );

      ArrayList<String> testCatalogFragmentFiles = kartaConfiguration.getTestCatalogFragmentFiles();

      if ( testCatalogFragmentFiles != null )
      {
         for ( String testCatalogFragmentFile : kartaConfiguration.getTestCatalogFragmentFiles() )
         {
            catalogFileText = FileUtils.readFileToString( new File( testCatalogFragmentFile ), Charset.defaultCharset() );
            testCategory = ( catalogFileText == null ) ? new TestCategory() : yamlObjectMapper.readValue( catalogFileText, TestCategory.class );
            testCatalogManager.mergeWithCatalog( testCategory );
         }
      }

      testCatalogManager.mergeRepositoryDirectoryIntoCatalog( new File( Constants.DOT ) );

      executorServiceManager = new ExecutorServiceManager();
      executorServiceManager.getOrAddExecutorServiceForGroup( Constants.__TESTS__, kartaConfiguration.getTestThreadCount() );

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Initialize bean registry
      /*---------------------------------------------------------------------------------------------------------------------*/
      beanRegistry = new BeanRegistry();
      beanRegistry.add( configurator );
      beanRegistry.add( testCatalogManager );
      beanRegistry.add( eventProcessor );
      beanRegistry.add( nodeRegistry );
      beanRegistry.add( executorServiceManager );
      beanRegistry.add( random );

      ArrayList<String> packagesToScanBeans = kartaConfiguration.getConfigurationScanPackages();
      if ( packagesToScanBeans != null )
      {
         beanRegistry.addBeansFromPackages( packagesToScanBeans );
      }
      /*---------------------------------------------------------------------------------------------------------------------*/
      // Initialize enabled plug-ins only after all other beans are initialized
      /*---------------------------------------------------------------------------------------------------------------------*/
      if ( !pnpRegistry.initializePlugins( beanRegistry, configurator ) )
      {
         return false;
      }

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Start event processor with event listener plug-ins*
      /*---------------------------------------------------------------------------------------------------------------------*/
      eventProcessor.start();

      return true;
   }

   public void addNodes()
   {
      ArrayList<KartaMinionConfiguration> nodes = kartaConfiguration.getNodes();

      for ( KartaMinionConfiguration node : nodes )
      {
         nodeRegistry.addNode( node );
      }
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

   public static HashMap<String, Serializable> getMergedTestData( String runName, HashMap<String, Serializable> stepTestData, HashMap<String, ArrayList<Serializable>> stepTestDataSet, ArrayList<TestDataSource> testDataSources,
                                                                  ExecutionStepPointer executionStepPointer )
            throws Throwable
   {
      HashMap<String, Serializable> mergedTestData = new HashMap<String, Serializable>();
      for ( TestDataSource tds : testDataSources )
      {
         HashMap<String, Serializable> testData = tds.getData( executionStepPointer );
         testData.forEach( ( key, value ) -> mergedTestData.put( key, value ) );
      }

      if ( stepTestDataSet != null )
      {
         long iterationIndex = ( executionStepPointer != null ) ? executionStepPointer.getIterationIndex() : 0;
         if ( iterationIndex <= 0 )
         {
            iterationIndex = 0;
         }
         for ( String dataKey : stepTestDataSet.keySet() )
         {
            ArrayList<Serializable> possibleValues = stepTestDataSet.get( dataKey );
            if ( ( possibleValues != null ) && !possibleValues.isEmpty() )
            {
               int valueIndex = (int) ( iterationIndex % possibleValues.size() );
               mergedTestData.put( dataKey, possibleValues.get( valueIndex ) );
            }
         }
      }

      if ( stepTestData != null )
      {
         stepTestData.forEach( ( key, value ) -> mergedTestData.put( key, value ) );
      }

      return mergedTestData;
   }

   public void loadRuntimeObjects( Object object ) throws IllegalArgumentException, IllegalAccessException
   {
      beanRegistry.loadBeans( object );
   }

   public FeatureResult runFeatureFile( String runName, String featureSourceParserPlugin, String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String featureFileName, boolean chanceBasedScenarioExecution, boolean exclusiveScenarioPerIteration,
                                        long numberOfIterations, int numberOfIterationsInParallel )
   {
      try
      {
         String featureSource = ClassPathLoaderUtils.readAllText( featureFileName );

         if ( StringUtils.isEmpty( featureSource ) )
         {
            String errorMsg = "Feature file invalid: " + featureFileName;
            log.error( errorMsg );
            return StandardFeatureResults.error( errorMsg );
         }
         return runFeatureSource( runName, featureSourceParserPlugin, stepRunnerPlugin, testDataSourcePlugins, featureSource, chanceBasedScenarioExecution, exclusiveScenarioPerIteration, numberOfIterations, numberOfIterationsInParallel );
      }
      catch ( Throwable t )
      {
         log.error( Constants.EMPTY_STRING, t );
         return StandardFeatureResults.error( t );
      }
   }

   public FeatureResult runFeatureSource( String runName, String featureFileSourceString, boolean chanceBasedScenarioExecution, boolean exclusiveScenarioPerIteration, long numberOfIterations, int numberOfIterationsInParallel )
   {
      return runFeatureSource( runName, kartaConfiguration.getDefaultFeatureSourceParserPlugin(), kartaConfiguration.getDefaultStepRunnerPlugin(), kartaConfiguration
               .getDefaultTestDataSourcePlugins(), featureFileSourceString, chanceBasedScenarioExecution, exclusiveScenarioPerIteration, numberOfIterations, numberOfIterationsInParallel );
   }

   public FeatureResult runFeatureSource( String runName, String featureSourceParserPlugin, String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String featureFileSourceString, boolean chanceBasedScenarioExecution,
                                          boolean exclusiveScenarioPerIteration, long numberOfIterations, int numberOfIterationsInParallel )
   {
      try
      {
         FeatureSourceParser featureParser = (FeatureSourceParser) pnpRegistry.getPlugin( featureSourceParserPlugin );

         if ( featureParser == null )
         {
            String errorMsg = "Failed to get a feature source parser of type: " + kartaConfiguration.getDefaultFeatureSourceParserPlugin();
            log.error( errorMsg );
            return StandardFeatureResults.error( errorMsg );
         }
         TestFeature testFeature = featureParser.parseFeatureSource( featureFileSourceString );

         return runFeature( stepRunnerPlugin, testDataSourcePlugins, runName, testFeature, chanceBasedScenarioExecution, exclusiveScenarioPerIteration, numberOfIterations, numberOfIterationsInParallel );
      }
      catch ( Throwable t )
      {
         log.error( Constants.EMPTY_STRING, t );
         return StandardFeatureResults.error( t );
      }
   }

   public FeatureResult runFeature( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, TestFeature feature, boolean chanceBasedScenarioExecution, boolean exclusiveScenarioPerIteration, long numberOfIterations,
                                    int numberOfIterationsInParallel )
   {
      StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( stepRunnerPlugin );

      if ( stepRunner == null )
      {
         String errorMsg = "Failed to get a step runner of type: " + kartaConfiguration.getDefaultStepRunnerPlugin();
         log.error( errorMsg );
         return StandardFeatureResults.error( errorMsg );
      }

      ArrayList<TestDataSource> testDataSources = new ArrayList<TestDataSource>();

      for ( String testDataSourcePlugin : testDataSourcePlugins )
      {
         TestDataSource testDataSource = (TestDataSource) pnpRegistry.getPlugin( testDataSourcePlugin );

         if ( testDataSource == null )
         {
            String errorMsg = "Failed to get a test data source of type: " + testDataSourcePlugin;
            log.error( errorMsg );
            return StandardFeatureResults.error( errorMsg );
         }

         testDataSources.add( testDataSource );
      }

      try
      {
         FeatureRunner featureRunner = FeatureRunner.builder().kartaRuntime( this ).stepRunner( stepRunner ).testDataSources( testDataSources ).chanceBasedScenarioExecution( chanceBasedScenarioExecution )
                  .exclusiveScenarioPerIteration( exclusiveScenarioPerIteration ).runName( runName ).testFeature( feature ).numberOfIterations( numberOfIterations ).numberOfIterationsInParallel( numberOfIterationsInParallel ).build();
         FeatureResult featureResult = featureRunner.call();
         return featureResult;
      }
      catch ( Throwable t )
      {
         log.error( Constants.EMPTY_STRING, t );
         return StandardFeatureResults.error( t );
      }
   }

   public boolean runTestTarget( String runName, RunTarget runTarget )
   {
      return runTestTarget( runName, kartaConfiguration.getDefaultStepRunnerPlugin(), kartaConfiguration.getDefaultStepRunnerPlugin(), kartaConfiguration.getDefaultTestDataSourcePlugins(), runTarget );
   }

   public ArrayList<TestDataSource> getTestDataSourcePlugins( HashSet<String> testDataSourcePlugins )
   {
      ArrayList<TestDataSource> testDataSources = new ArrayList<TestDataSource>();
      for ( String testDataSourcePlugin : testDataSourcePlugins )
      {
         TestDataSource testDataSource = (TestDataSource) pnpRegistry.getPlugin( testDataSourcePlugin );

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
            eventProcessor.runStart( runName );
            eventProcessor.raiseEvent( new RunStartEvent( runName ) );
            FeatureResult result = runFeatureFile( runName, featureSourceParserPlugin, stepRunnerPlugin, testDataSourcePlugins, runTarget.getFeatureFile(), runTarget.getChanceBasedScenarioExecution(), runTarget.getExclusiveScenarioPerIteration(), runTarget
                     .getNumberOfIterations(), runTarget.getNumberOfThreads() );
            eventProcessor.raiseEvent( new RunCompleteEvent( runName ) );
            eventProcessor.runStop( runName );
            return result.isPassed();
         }
         else if ( StringUtils.isNotBlank( runTarget.getJavaTest() ) )
         {
            ArrayList<TestDataSource> testDataSources = getTestDataSourcePlugins( testDataSourcePlugins );
            if ( testDataSources == null )
            {
               return false;
            }

            JavaFeatureRunner testRunner = JavaFeatureRunner.builder().kartaRuntime( this ).testDataSources( testDataSources ).runName( runName ).javaTest( runTarget.getJavaTest() ).javaTestJarFile( runTarget.getJavaTestJarFile() )
                     .numberOfIterations( runTarget.getNumberOfIterations() ).numberOfIterationsInParallel( runTarget.getNumberOfThreads() ).chanceBasedScenarioExecution( runTarget.getChanceBasedScenarioExecution() )
                     .exclusiveScenarioPerIteration( runTarget.getExclusiveScenarioPerIteration() ).build();
            eventProcessor.runStart( runName );
            eventProcessor.raiseEvent( new RunStartEvent( runName ) );
            FeatureResult result = testRunner.call();
            eventProcessor.raiseEvent( new RunCompleteEvent( runName ) );
            eventProcessor.runStop( runName );
            return result.isPassed();
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
         log.error( Constants.EMPTY_STRING, t );
         return false;
      }
   }

   public boolean runTestsWithTags( String runName, HashSet<String> tags ) throws Throwable
   {
      eventProcessor.runStart( runName );
      eventProcessor.raiseEvent( new RunStartEvent( runName ) );
      ArrayList<Test> tests = testCatalogManager.filterTestsByTag( tags );
      Collections.sort( tests );
      boolean result = runTest( runName, tests );
      eventProcessor.raiseEvent( new RunCompleteEvent( runName ) );
      eventProcessor.runStop( runName );
      return result;
   }

   public boolean runTest( String runName, Collection<Test> tests ) throws Throwable
   {
      ArrayList<Future<FeatureResult>> futures = new ArrayList<Future<FeatureResult>>();

      AtomicBoolean successful = new AtomicBoolean( true );

      for ( Test test : tests )
      {
         switch ( test.getTestType() )
         {
            case FEATURE:
               FeatureSourceParser featureParser = (FeatureSourceParser) pnpRegistry.getPlugin( test.getFeatureSourceParserPlugin() );

               if ( featureParser == null )
               {
                  log.error( "Failed to get a feature source parser of type: " + test.getFeatureSourceParserPlugin() );
                  return false;
               }
               // TODO: Handle io errors
               TestFeature testFeature = featureParser.parseFeatureSource( IOUtils.toString( DynamicClassLoader.getClassPathResourceInJarAsStream( test.getSourceArchive(), test.getFeatureFileName() ), Charset.defaultCharset() ) );

               StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( test.getStepRunnerPlugin() );

               if ( stepRunner == null )
               {
                  log.error( "Failed to get a step runner of type: " + test.getStepRunnerPlugin() );
                  return false;
               }

               ArrayList<TestDataSource> featureTestDataSources = new ArrayList<TestDataSource>();

               for ( String testDataSourcePlugin : test.getTestDataSourcePlugins() )
               {
                  TestDataSource testDataSource = (TestDataSource) pnpRegistry.getPlugin( testDataSourcePlugin );

                  if ( testDataSource == null )
                  {
                     log.error( "Failed to get a test data source of type: " + testDataSourcePlugin );
                     return false;
                  }

                  featureTestDataSources.add( testDataSource );
               }

               ExecutorService testExecutorService = executorServiceManager.getExecutorServiceForGroup( test.getThreadGroup() );

               FeatureRunner featureRunner = FeatureRunner.builder().kartaRuntime( this ).stepRunner( stepRunner ).testDataSources( featureTestDataSources ).chanceBasedScenarioExecution( test.getChanceBasedScenarioExecution() )
                        .exclusiveScenarioPerIteration( test.getExclusiveScenarioPerIteration() ).runName( runName ).testFeature( testFeature ).numberOfIterations( test.getNumberOfIterations() ).numberOfIterationsInParallel( test.getNumberOfThreads() )
                        .resultConsumer( ( result ) -> successful.set( result.isSuccessful() && successful.get() ) ).tags( test.getTags() ).build();

               futures.add( testExecutorService.submit( featureRunner ) );
               break;

            case JAVA_TEST:
               ArrayList<TestDataSource> javaTestDataSources = new ArrayList<TestDataSource>();
               for ( String testDataSourcePlugin : test.getTestDataSourcePlugins() )
               {
                  TestDataSource testDataSource = (TestDataSource) pnpRegistry.getPlugin( testDataSourcePlugin );

                  if ( testDataSource == null )
                  {
                     log.error( "Failed to get a test data source of type: " + testDataSourcePlugin );
                     return false;
                  }

                  javaTestDataSources.add( testDataSource );
               }
               JavaFeatureRunner testRunner = JavaFeatureRunner.builder().kartaRuntime( this ).testDataSources( javaTestDataSources ).runName( runName ).javaTest( test.getJavaTestClass() ).javaTestJarFile( test.getSourceArchive() )
                        .numberOfIterations( test.getNumberOfIterations() ).numberOfIterationsInParallel( test.getNumberOfThreads() ).chanceBasedScenarioExecution( test.getChanceBasedScenarioExecution() )
                        .exclusiveScenarioPerIteration( test.getExclusiveScenarioPerIteration() ).resultConsumer( ( result ) -> successful.set( result.isSuccessful() && successful.get() ) ).build();
               testExecutorService = executorServiceManager.getExecutorServiceForGroup( test.getThreadGroup() );
               futures.add( testExecutorService.submit( testRunner ) );
               break;
         }
      }

      for ( Future<FeatureResult> future : futures )
      {
         future.get();
      }

      return successful.get();
   }

   public StepResult runStep( String stepRunnerPlugin, TestStep testStep, TestExecutionContext testExecutionContext ) throws TestFailureException
   {
      StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( stepRunnerPlugin );
      if ( stepRunner == null )
      {
         return StandardStepResults.error( TestIncident.builder().message( "Step runner plugin not found: " + stepRunnerPlugin ).build() );
      }
      return stepRunner.runStep( testStep, testExecutionContext );
   }

   public ScenarioResult runTestScenario( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, String featureName, long iterationIndex, ArrayList<TestStep> scenarioSetupSteps, TestScenario testScenario,
                                          ArrayList<TestStep> scenarioTearDownSteps, long scenarioIterationNumber )
   {
      StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( stepRunnerPlugin );
      ArrayList<TestDataSource> testDataSources = getTestDataSourcePlugins( testDataSourcePlugins );
      if ( ( stepRunner == null ) || ( testDataSources == null ) )
      {
         log.error( "Plugin(s) not found: " + stepRunnerPlugin + testDataSourcePlugins );
         return StandardScenarioResults.error( TestIncident.builder().message( "Plugin(s) not found: " + stepRunnerPlugin + testDataSourcePlugins ).build() );
      }

      return runTestScenario( stepRunner, testDataSources, runName, featureName, iterationIndex, scenarioSetupSteps, testScenario, scenarioTearDownSteps, scenarioIterationNumber );
   }

   public ScenarioResult runTestScenario( StepRunner stepRunner, ArrayList<TestDataSource> testDataSources, String runName, String featureName, long iterationIndex, ArrayList<TestStep> scenarioSetupSteps, TestScenario testScenario,
                                          ArrayList<TestStep> scenarioTearDownSteps, long scenarioIterationNumber )
   {
      ScenarioRunner scenarioRunner = ScenarioRunner.builder().kartaRuntime( this ).stepRunner( stepRunner ).testDataSources( testDataSources ).runName( runName ).featureName( featureName ).iterationIndex( iterationIndex )
               .scenarioSetupSteps( scenarioSetupSteps ).testScenario( testScenario ).scenarioTearDownSteps( scenarioTearDownSteps ).scenarioIterationNumber( scenarioIterationNumber ).build();
      return scenarioRunner.call();
   }

   public StepResult runChaosAction( String stepRunnerPlugin, ChaosAction chaosAction, TestExecutionContext context ) throws TestFailureException
   {
      StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( stepRunnerPlugin );
      if ( stepRunner == null )
      {
         return StandardStepResults.error( TestIncident.builder().message( "Step runner plugin not found: " + stepRunnerPlugin ).build() );
      }
      return stepRunner.performChaosAction( chaosAction, context );
   }

   public long scheduleJob( String stepRunnerPlugin, HashSet<String> testDataSourcePlugins, String runName, String featureName, TestJob job ) throws Throwable
   {
      StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( stepRunnerPlugin );
      ArrayList<TestDataSource> testDataSources = getTestDataSourcePlugins( testDataSourcePlugins );
      if ( ( stepRunner == null ) || ( testDataSources == null ) )
      {
         log.error( "Plugin(s) not found: " + stepRunnerPlugin + testDataSourcePlugins );
         return -1;
      }
      return startScheduledJob( stepRunner, testDataSources, runName, featureName, job );
   }

   public long startScheduledJob( StepRunner stepRunner, ArrayList<TestDataSource> testDataSources, String runName, String featureName, TestJob job ) throws Throwable
   {
      long jobInterval = job.getInterval();
      int repeatCount = job.getIterationCount();

      if ( jobInterval > 0 )
      {
         HashMap<String, Object> jobData = new HashMap<String, Object>();
         jobData.put( Constants.KARTA_RUNTIME, this );
         jobData.put( Constants.STEP_RUNNER, stepRunner );
         jobData.put( Constants.TEST_DATA_SOURCES, testDataSources );
         jobData.put( Constants.RUN_NAME, runName );
         jobData.put( Constants.FEATURE_NAME, featureName );
         jobData.put( Constants.TEST_JOB, job );
         jobData.put( Constants.ITERATION_COUNTER, new AtomicLong() );
         return QuartzJobScheduler.scheduleJob( QuartzTestJob.class, jobInterval, repeatCount, jobData );
      }
      else
      {
         TestJobRunner.run( this, stepRunner, testDataSources, runName, featureName, job, 0 );
         return -1;
      }
   }

   public boolean deleteJob( Long jobId )
   {
      return QuartzJobScheduler.deleteJob( jobId );
   }

   public boolean deleteJobs( ArrayList<Long> jobIds )
   {
      return QuartzJobScheduler.deleteJobs( jobIds );
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
