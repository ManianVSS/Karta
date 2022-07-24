package org.mvss.karta.framework.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.configuration.KartaConfiguration;
import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.chaos.ChaosActionTreeNode;
import org.mvss.karta.framework.core.*;
import org.mvss.karta.framework.nodes.IKartaNodeRegistry;
import org.mvss.karta.framework.nodes.KartaNode;
import org.mvss.karta.framework.nodes.KartaNodeConfiguration;
import org.mvss.karta.framework.randomization.ObjectGenerationRule;
import org.mvss.karta.framework.runtime.event.*;
import org.mvss.karta.framework.runtime.interfaces.*;
import org.mvss.karta.framework.runtime.testcatalog.Test;
import org.mvss.karta.framework.runtime.testcatalog.TestCatalogManager;
import org.mvss.karta.framework.runtime.testcatalog.TestCategory;
import org.mvss.karta.framework.threading.BlockingRunnableQueue;
import org.mvss.karta.framework.utils.*;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Log4j2
@AllArgsConstructor
public class KartaRuntime implements AutoCloseable
{
   @Getter
   private Random random = new Random();

   @Getter
   private KartaConfiguration kartaConfiguration;

   @Getter
   private PnPRegistry pnpRegistry;

   @Getter
   private Configurator configurator;

   @Getter
   private TestCatalogManager testCatalogManager;

   @Getter
   private EventProcessor eventProcessor;

   @Getter
   private IKartaNodeRegistry nodeRegistry;

   private static final ObjectMapper yamlObjectMapper = ParserUtils.getYamlObjectMapper();

   @Getter
   private BeanRegistry beanRegistry;

   @Getter
   private ExecutorServiceManager executorServiceManager;

   @Getter
   private final List<AutoCloseable> autoCloseables = Collections.synchronizedList( new ArrayList<>() );

   /**
    * This flag is used to run a custom Karta Minion server by disabling node initializing first and call addNodes later
    */
   public static boolean initializeNodes = true;

   @Getter
   private RunInfo defaultRunInfo = null;

   /**
    * Default constructor is reserved for default runtime instance. Use getInstance.
    */
   private KartaRuntime()
   {

   }

   private static KartaRuntime instance = null;

   private static final Object _syncLockObject = new Object();

   /**
    * Gets the default KartaRuntime singleton instance.
    */
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

   /**
    * Initializes the runtime with the default settings
    */
   public boolean initializeRuntime() throws Exception
   {
      /*---------------------------------------------------------------------------------------------------------------------*/
      // Initialize karta configuration
      /*---------------------------------------------------------------------------------------------------------------------*/
      String configString = ClassPathLoaderUtils.readAllText( Constants.KARTA_CONFIGURATION_YAML );

      if ( configString == null )
      {
         if ( PropertyUtils.systemPropertyMap.containsKey( Constants.KARTA_HOME ) )
         {
            // TODO: Move all configuration file to user directory
            File kartaHomeConfigFile = Paths.get( PropertyUtils.systemPropertyMap.get( Constants.KARTA_HOME ), Constants.BIN,
                     Constants.KARTA_CONFIGURATION_YAML ).toFile();
            if ( kartaHomeConfigFile.exists() )
            {
               configString = FileUtils.readFileToString( kartaHomeConfigFile, Charset.defaultCharset() );
            }
         }
      }

      if ( ( configString != null ) && StringUtils.isNotEmpty( configString.trim() ) )
      {
         try
         {
            kartaConfiguration = yamlObjectMapper.readValue( configString, KartaConfiguration.class );
         }
         catch ( Exception e )
         {
            log.error( "Error while reading Karta configuration yaml string " + configString, e );
            return false;
         }
      }

      if ( kartaConfiguration == null )
      {
         kartaConfiguration = KartaConfiguration.getDefaultConfiguration();
      }

      log.debug( "Karta configuration loaded as: " + kartaConfiguration );
      configString = ClassPathLoaderUtils.readAllText( Constants.KARTA_CONFIGURATION_OVERRIDES_YAML );
      if ( configString != null )
      {
         try
         {
            KartaConfiguration kartaOverrideConfiguration = yamlObjectMapper.readValue( configString, KartaConfiguration.class );
            log.debug( "Karta configuration override loaded as: " + kartaOverrideConfiguration );
            kartaConfiguration.overrideConfiguration( kartaOverrideConfiguration );
         }
         catch ( Exception e )
         {
            log.error( "Error while reading Karta configuration yaml string " + configString, e );
            return false;
         }
      }

      kartaConfiguration.expandSystemAndEnvProperties();

      log.info( "Karta configuration after override and expansion is: " + kartaConfiguration );

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Initialize random
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
      configurator.mergeProperties( kartaConfiguration.getProperties() );
      ArrayList<String> propertiesFileList = kartaConfiguration.getPropertyFiles();
      if ( propertiesFileList == null )
      {
         propertiesFileList = new ArrayList<>();
      }

      if ( !propertiesFileList.isEmpty() )
      {
         propertiesFileList.add( Constants.KARTA_RUNTIME_PROPERTIES_YAML );
      }

      String[] propertyFilesToLoad = new String[propertiesFileList.size()];
      propertiesFileList.toArray( propertyFilesToLoad );
      if ( !configurator.mergePropertiesFiles( propertyFilesToLoad ) )
      {
         log.error( "Error while merging property files to configurator store." );
         return false;
      }

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Load and enable plug-ins
      /*---------------------------------------------------------------------------------------------------------------------*/
      pnpRegistry = new PnPRegistry();
      pnpRegistry.addPluginConfiguration( kartaConfiguration.getPluginConfigurations() );

      pnpRegistry.enablePlugins( kartaConfiguration.getEnabledPlugins() );

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Initialize event processor
      /*---------------------------------------------------------------------------------------------------------------------*/
      eventProcessor = new EventProcessor();
      configurator.loadProperties( eventProcessor );
      for ( Plugin plugin : pnpRegistry.getEnabledPluginsOfType( TestEventListener.class ) )
      {
         if ( !eventProcessor.addEventListener( (TestEventListener) plugin ) )
         {
            return false;
         }
      }

      for ( Plugin plugin : pnpRegistry.getEnabledPluginsOfType( TestLifeCycleHook.class ) )
      {
         if ( !eventProcessor.addLifeCycleHook( (TestLifeCycleHook) plugin ) )
         {
            return false;
         }
      }

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Initialize node registry
      /*---------------------------------------------------------------------------------------------------------------------*/
      // TODO: Add task pulling worker minions to support minions as clients rather than open server sockets

      nodeRegistry = kartaConfiguration.createNodeRegistry();

      if ( initializeNodes )
      {
         addNodes();
      }

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Initialize Test catalog manager and load test catalog
      /*---------------------------------------------------------------------------------------------------------------------*/
      testCatalogManager = new TestCatalogManager();
      String catalogFileText = ClassPathLoaderUtils.readAllText( Constants.TEST_CATALOG_FILE_NAME );
      TestCategory testCategory = ( catalogFileText == null ) ?
               new TestCategory() :
               yamlObjectMapper.readValue( catalogFileText, TestCategory.class );
      testCatalogManager.mergeWithCatalog( testCategory );

      ArrayList<String> testCatalogFragmentFiles = kartaConfiguration.getTestCatalogFragmentFiles();

      if ( testCatalogFragmentFiles != null )
      {
         for ( String testCatalogFragmentFile : kartaConfiguration.getTestCatalogFragmentFiles() )
         {
            catalogFileText = ClassPathLoaderUtils.readAllText( testCatalogFragmentFile );
            testCategory    = ( catalogFileText == null ) ? new TestCategory() : yamlObjectMapper.readValue( catalogFileText, TestCategory.class );
            testCatalogManager.mergeWithCatalog( testCategory );
         }
      }

      testCatalogManager.mergeRepositoryDirectoryIntoCatalog( new File( Constants.DOT ) );

      executorServiceManager = new ExecutorServiceManager();
      executorServiceManager.addExecutorServiceForGroups( kartaConfiguration.getThreadGroups() );
      executorServiceManager.getOrAddExecutorServiceForGroup( Constants.__DEFAULT__, 1 );

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Initialize bean registry
      /*---------------------------------------------------------------------------------------------------------------------*/
      beanRegistry = new BeanRegistry();
      beanRegistry.add( this );
      beanRegistry.add( configurator );
      beanRegistry.add( testCatalogManager );
      beanRegistry.add( eventProcessor );
      beanRegistry.add( nodeRegistry );
      beanRegistry.add( executorServiceManager );
      beanRegistry.add( random );

      ArrayList<String> packagesToScanBeans = kartaConfiguration.getConfigurationScanPackages();
      if ( packagesToScanBeans != null )
      {
         processConfigBeans( packagesToScanBeans );
      }
      /*---------------------------------------------------------------------------------------------------------------------*/
      // Initialize enabled plug-ins only after all other beans are initialized
      /*---------------------------------------------------------------------------------------------------------------------*/

      for ( Plugin pluginEntry : pnpRegistry.getEnabledPlugins().values() )
      {
         initializeObject( pluginEntry );
      }
      /*---------------------------------------------------------------------------------------------------------------------*/
      // Start event processor with event listener plug-ins*
      /*---------------------------------------------------------------------------------------------------------------------*/
      eventProcessor.start();

      defaultRunInfo = RunInfo.builder().runName( Constants.UNNAMED ).featureSourceParserPlugin( kartaConfiguration.getDefaultFeatureSourceParser() )
               .stepRunnerPluginName( kartaConfiguration.getDefaultStepRunner() )
               .testDataSourcePlugins( kartaConfiguration.getDefaultTestDataSources() ).build();

      return true;
   }

   private static final List<Class<?>> configuredBeanClasses = Collections.synchronizedList( new ArrayList<>() );

   private final Consumer<Method> processBeanDefinition = new Consumer<>()
   {
      @Override
      public void accept( Method beanDefinitionMethod )
      {
         try
         {
            for ( KartaBean kartaBean : beanDefinitionMethod.getAnnotationsByType( KartaBean.class ) )
            {
               Class<?> beanDeclaringClass = beanDefinitionMethod.getDeclaringClass();
               initializeClass( beanDeclaringClass );
               String beanName = DataUtils.pickString( StringUtils::isNotEmpty, kartaBean.name(), kartaBean.value(),
                        beanDefinitionMethod.getReturnType().getName() );
               Class<?>[] paramTypes = beanDefinitionMethod.getParameterTypes();

               Object beanObj;

               if ( paramTypes.length == 0 )
               {
                  beanObj = beanDefinitionMethod.invoke( null );
               }
               else
               {
                  continue;
               }

               if ( StringUtils.isAllBlank( beanName ) )
               {
                  beanName = beanObj.getClass().getName();
               }

               if ( !beanRegistry.add( beanName, beanObj ) )
               {
                  log.error( "Bean: " + beanName + " is already registered." );
               }
               else
               {
                  log.info( "Bean: " + beanName + " registered." );
               }
            }
         }
         catch ( Throwable t )
         {
            log.error( "Exception while parsing bean definition from method  " + beanDefinitionMethod.getName(), t );
         }

      }
   };

   private final Consumer<Class<?>> processLoadPropertiesDefinition = classesToLoadPropertiesWith -> {
      try
      {
         initializeClass( classesToLoadPropertiesWith );
      }
      catch ( Throwable t )
      {
         log.error( "Exception while loading static fields from properties for class  " + classesToLoadPropertiesWith.getName(), t );
      }

   };

   public void processConfigBeans( Collection<String> configurationScanPackageNames )
   {
      AnnotationScanner.forEachMethod( configurationScanPackageNames, KartaBean.class, AnnotationScanner.IS_PUBLIC_AND_STATIC,
               AnnotationScanner.IS_NON_VOID_TYPE, AnnotationScanner.DOES_NOT_HAVE_PARAMETERS, processBeanDefinition );
      AnnotationScanner.forEachClass( configurationScanPackageNames, LoadConfiguration.class, AnnotationScanner.IS_PUBLIC,
               processLoadPropertiesDefinition );
   }

   private final ObjectMethodConsumer callObjectInitializer = ( object, method ) -> {
      try
      {
         method.invoke( object );
      }
      catch ( Throwable t )
      {
         log.error( "Exception while parsing bean definition from method  " + method.getName(), t );
      }
   };

   /**
    * Sets the properties and beans(static and non-static) for the object and calls any initializer methods
    */
   public void initializeObject( Object object )
   {
      try
      {
         Class<?> classOfObject = object.getClass();
         initializeClass( classOfObject );
         // Not checking one time initialization for object level here which can prevent garbage collection
         configurator.loadProperties( object );
         beanRegistry.loadBeans( object );
         AnnotationScanner.forEachMethod( object, Initializer.class, AnnotationScanner.IS_NON_STATIC, null,
                  AnnotationScanner.DOES_NOT_HAVE_PARAMETERS, callObjectInitializer );
      }
      catch ( Throwable t )
      {
         log.error( "Exception while initializing object", t );
      }
   }

   private final ClassMethodConsumer callClassInitializer = ( classToWorkWith, beanDefinitionMethod ) -> {
      try
      {
         beanDefinitionMethod.invoke( null );
      }
      catch ( Throwable t )
      {
         log.error( "Exception while calling initialization method for " + classToWorkWith.getName() + Constants.DOT + beanDefinitionMethod.getName(),
                  t );
      }

   };

   /**
    * Sets the properties and beans (static) for the class of object and calls any initializer methods
    */
   public void initializeClass( Class<?> theClassOfObject )
   {
      try
      {
         if ( !configuredBeanClasses.contains( theClassOfObject ) )
         {
            configurator.loadProperties( theClassOfObject );
            beanRegistry.loadStaticBeans( theClassOfObject );

            AnnotationScanner.forEachMethod( theClassOfObject, Initializer.class, AnnotationScanner.IS_STATIC, null,
                     AnnotationScanner.DOES_NOT_HAVE_PARAMETERS, callClassInitializer );
            configuredBeanClasses.add( theClassOfObject );
         }
      }
      catch ( Throwable t )
      {
         log.error( "Exception while initializing object", t );
      }
   }

   /**
    * <b> This is typically required only when creating a customized KartaMinionServer</b>. </br>
    * Adds the nodes based on the configuration. </br>
    * This needs to be called after initializing the runtime. </br>
    */
   public boolean addNodes()
   {
      boolean result = true;

      for ( KartaNodeConfiguration node : kartaConfiguration.getNodes() )
      {
         result = result && nodeRegistry.addNode( node );
      }
      return result;
   }

   /**
    * Auto-closable method for the runtime. Stops all tests and Karta components.
    */
   @Override
   public void close()
   {
      try
      {
         if ( executorServiceManager != null )
         {
            executorServiceManager.close();
            executorServiceManager = null;
         }

         QuartzJobScheduler.shutdown();

         if ( eventProcessor != null )
         {
            eventProcessor.close();
            eventProcessor = null;
         }

         if ( pnpRegistry != null )
         {
            pnpRegistry.close();
            pnpRegistry = null;
         }

         if ( nodeRegistry != null )
         {
            nodeRegistry.close();
            nodeRegistry = null;
         }

         for ( AutoCloseable autoCloseable : autoCloseables )
         {
            autoCloseable.close();
         }
         autoCloseables.clear();
      }
      catch ( Exception e )
      {
         log.error( e );
      }
   }

   private static final HashMap<String, FeatureSourceParser>                featureSourceParserMap = new HashMap<>();
   private static final HashMap<String, StepRunner>                         stepRunnerMap          = new HashMap<>();
   private static final HashMap<HashSet<String>, ArrayList<TestDataSource>> testDataSourcesMap     = new HashMap<>();

   private final Object fspMapLock = new Object();
   private final Object srMapLock  = new Object();
   private final Object tdsMapLock = new Object();

   /**
    * Returns the FeatureSourceParser based on the feature source parser plugin name provided.
    */
   public FeatureSourceParser getFeatureSourceParser( String featureSourceParserName )
   {
      synchronized ( fspMapLock )
      {

         if ( featureSourceParserName == null )
         {
            return null;
         }

         try
         {
            if ( !featureSourceParserMap.containsKey( featureSourceParserName ) )
            {
               FeatureSourceParser featureSourceParser = (FeatureSourceParser) pnpRegistry.getEnabledPlugin( featureSourceParserName );

               if ( featureSourceParser == null )
               {
                  return null;
               }
               featureSourceParserMap.put( featureSourceParserName, featureSourceParser );
            }
         }
         catch ( Throwable t )
         {
            log.error( "", t );
            return null;
         }

         return featureSourceParserMap.get( featureSourceParserName );
      }
   }

   /**
    * Returns the FeatureSourceParser based on the RunInfo
    */
   public FeatureSourceParser getFeatureSourceParser( RunInfo runInfo )
   {
      String featureSourceParserName = runInfo.getFeatureSourceParserPlugin();
      return getFeatureSourceParser( featureSourceParserName );
   }

   /**
    * Returns the StepRunner based on the step runner plugin name provided.
    */
   public StepRunner getStepRunner( String stepRunnerPluginName )
   {
      synchronized ( srMapLock )
      {
         if ( stepRunnerPluginName == null )
         {
            return null;
         }

         try
         {
            if ( !stepRunnerMap.containsKey( stepRunnerPluginName ) )
            {
               StepRunner stepRunner = (StepRunner) pnpRegistry.getEnabledPlugin( stepRunnerPluginName );

               if ( stepRunner == null )
               {
                  return null;
               }
               stepRunnerMap.put( stepRunnerPluginName, stepRunner );
            }
         }
         catch ( Throwable t )
         {
            log.error( "", t );
            return null;
         }

         return stepRunnerMap.get( stepRunnerPluginName );
      }
   }

   /**
    * Returns the StepRunner based on the RunInfo
    */
   public StepRunner getStepRunner( RunInfo runInfo )
   {
      String stepRunnerPluginName = runInfo.getStepRunnerPluginName();
      return getStepRunner( stepRunnerPluginName );
   }

   /**
    * Returns a list of TestDataSources based on the set of test data source plugin names provided
    */
   public ArrayList<TestDataSource> getTestDataSources( HashSet<String> testDataSourcesPluginNames )
   {
      synchronized ( tdsMapLock )
      {
         if ( testDataSourcesPluginNames == null )
         {
            return null;
         }

         try
         {
            if ( !testDataSourcesMap.containsKey( testDataSourcesPluginNames ) )
            {
               ArrayList<TestDataSource> testDataSources = new ArrayList<>();

               for ( String testDataSourcePlugin : testDataSourcesPluginNames )
               {
                  TestDataSource testDataSource = (TestDataSource) pnpRegistry.getEnabledPlugin( testDataSourcePlugin );

                  if ( testDataSource == null )
                  {
                     return null;
                  }

                  testDataSources.add( testDataSource );
               }
               testDataSourcesMap.put( testDataSourcesPluginNames, testDataSources );
            }
         }
         catch ( Throwable t )
         {
            log.error( "", t );
            return null;
         }

         return testDataSourcesMap.get( testDataSourcesPluginNames );
      }
   }

   /**
    * Returns a list of TestDataSources based on the RunInfo
    */
   public ArrayList<TestDataSource> getTestDataSources( RunInfo runInfo )
   {
      HashSet<String> testDataSourcesPluginNames = runInfo.getTestDataSourcePlugins();
      return getTestDataSources( testDataSourcesPluginNames );
   }

   /**
    * Runs a RunTarget and returns if the feature/JavaTestCase or Tags passed
    */
   public RunResult runTestTarget( RunInfo runInfo, RunTarget runTarget )
   {
      RunResult runResult = new RunResult();

      runInfo.setDefaultPlugins( kartaConfiguration.getDefaultFeatureSourceParser(), kartaConfiguration.getDefaultStepRunner(),
               kartaConfiguration.getDefaultTestDataSources() );

      try
      {
         String runName = runInfo.getRunName();

         HashSet<String> individualTestTags = new HashSet<>();
         individualTestTags.add( Constants.__ALL__ );

         if ( StringUtils.isNotBlank( runTarget.getFeatureFile() ) )
         {
            if ( !eventProcessor.runStart( runName, individualTestTags ) )
            {
               runResult.setError( true );
               runResult.setEndTime( new Date() );
               return runResult;
            }

            eventProcessor.raiseEvent( new RunStartEvent( runName ) );
            FeatureResult result = runFeatureFile( runInfo, runTarget.getFeatureFile() );
            runResult.setEndTime( new Date() );
            runResult.addTestResult( result );
            eventProcessor.raiseEvent( new RunCompleteEvent( runName, runResult ) );

            if ( !eventProcessor.runStop( runName, individualTestTags ) )
            {
               runResult.setError( true );
               runResult.setEndTime( new Date() );
               return runResult;
            }
            return runResult;
         }
         else if ( StringUtils.isNotBlank( runTarget.getJavaTest() ) )
         {
            ArrayList<TestDataSource> testDataSources = getTestDataSources( runInfo );
            if ( testDataSources == null )
            {
               runResult.setEndTime( new Date() );
               runResult.setSuccessful( false );
               return runResult;
            }

            JavaFeatureRunner testRunner = JavaFeatureRunner.builder().kartaRuntime( this ).runInfo( runInfo ).javaTest( runTarget.getJavaTest() )
                     .javaTestJarFile( runTarget.getJavaTestJarFile() ).build();
            if ( !eventProcessor.runStart( runName, individualTestTags ) )
            {
               runResult.setError( true );
               runResult.setEndTime( new Date() );
               return runResult;
            }
            eventProcessor.raiseEvent( new RunStartEvent( runName ) );
            FeatureResult result = testRunner.call();
            runResult.setEndTime( new Date() );
            runResult.addTestResult( result );
            eventProcessor.raiseEvent( new RunCompleteEvent( runName, runResult ) );
            if ( !eventProcessor.runStop( runName, individualTestTags ) )
            {
               runResult.setError( true );
               runResult.setEndTime( new Date() );
               return runResult;
            }
            return runResult;
         }
         else if ( ( runTarget.getRunTags() != null && !runTarget.getRunTags().isEmpty() ) )
         {
            return runTestsWithTags( runInfo, runTarget.getRunTags() );
         }
         else
         {
            runResult.setEndTime( new Date() );
            runResult.setSuccessful( false );
            return runResult;
         }
      }
      catch ( Throwable t )
      {
         log.error( Constants.EMPTY_STRING, t );
         runResult.setEndTime( new Date() );
         runResult.setSuccessful( false );
         return runResult;
      }
   }

   /**
    * Runs tests filtered from the TestCatalog using the set of tags provided, uses minions if configured and returns if all the tests passed.
    */
   public RunResult runTestsWithTags( RunInfo runInfo, HashSet<String> tags ) throws Throwable
   {
      RunResult runResult = new RunResult();

      String runName = runInfo.getRunName();

      if ( !eventProcessor.runStart( runName, tags ) )
      {
         runResult.setError( true );
         runResult.setEndTime( new Date() );
         return runResult;
      }

      eventProcessor.raiseEvent( new RunStartEvent( runName ) );
      ArrayList<Test> tests = testCatalogManager.filterTestsByTag( tags );
      Collections.sort( tests );
      runResult = runTest( runInfo, tests );
      eventProcessor.raiseEvent( new RunCompleteEvent( runName, runResult ) );

      if ( !eventProcessor.runStop( runName, tags ) )
      {
         runResult.setError( true );
         runResult.setEndTime( new Date() );
         return runResult;
      }

      return runResult;
   }

   /**
    * Runs a collection of Tests, uses minions if configured and returns if all the tests passed.
    */
   public RunResult runTest( RunInfo runInfo, Collection<Test> tests ) throws Throwable
   {
      RunResult                        result  = new RunResult();
      ArrayList<Future<FeatureResult>> futures = new ArrayList<>();

      for ( Test test : tests )
      {
         switch ( test.getTestType() )
         {
            case FEATURE:
               RunInfo runInfoForTest = runInfo.getRunInfoForTest( test );
               FeatureSourceParser featureParser = getFeatureSourceParser( runInfoForTest );

               if ( featureParser == null )
               {
                  log.error( "Failed to get a feature source parser of type: " + runInfoForTest.getFeatureSourceParserPlugin() );
                  result.setEndTime( new Date() );
                  result.setSuccessful( false );
                  return result;
               }

               StepRunner stepRunner = getStepRunner( runInfoForTest );
               if ( stepRunner == null )
               {
                  log.error( "Failed to get a step runner for run: " + runInfo );
                  result.setEndTime( new Date() );
                  result.setSuccessful( false );
                  return result;
               }

               ArrayList<TestDataSource> testDataSources = getTestDataSources( runInfo );
               if ( testDataSources == null )
               {
                  log.error( "Failed to get test data sources for run: " + runInfo );
                  result.setEndTime( new Date() );
                  result.setSuccessful( false );
                  return result;
               }

               String sourceArchive = test.getSourceArchive();
               String featureFileName = test.getFeatureFileName();

               if ( featureFileName == null )
               {
                  log.error( "Feature file missing for test: " + test );
                  continue;
               }

               String featureSourceCode = null;

               if ( ( sourceArchive == null ) || !Files.exists( Paths.get( sourceArchive ) ) )
               {
                  featureSourceCode = ClassPathLoaderUtils.readAllText( featureFileName );

                  if ( featureSourceCode == null )
                  {
                     log.error( "Could not load feature source file " + featureFileName + " from classpath or source archive " + sourceArchive );
                     continue;
                  }
               }
               else
               {
                  InputStream jarFileStream = DynamicClassLoader.getClassPathResourceInJarAsStream( sourceArchive, featureFileName );

                  if ( jarFileStream != null )
                  {
                     featureSourceCode = IOUtils.toString( jarFileStream, Charset.defaultCharset() );
                  }
               }

               if ( featureSourceCode == null )
               {
                  log.error( "Could not load feature file for test " + test );
                  continue;
               }

               TestFeature testFeature = featureParser.parseFeatureSource( featureSourceCode );

               ExecutorService testExecutorService = executorServiceManager.getExecutorServiceForGroup( test.getThreadGroup() );

               FeatureRunner featureRunner = FeatureRunner.builder().kartaRuntime( this ).runInfo( runInfoForTest ).testFeature( testFeature )
                        .resultConsumer( result::addTestResult ).build();

               futures.add( testExecutorService.submit( featureRunner ) );
               break;

            case JAVA_TEST:
               JavaFeatureRunner testRunner = JavaFeatureRunner.builder().kartaRuntime( this ).runInfo( runInfo ).javaTest( test.getJavaTestClass() )
                        .javaTestJarFile( test.getSourceArchive() ).resultConsumer( result::addTestResult ).build();
               testExecutorService = executorServiceManager.getExecutorServiceForGroup( test.getThreadGroup() );
               futures.add( testExecutorService.submit( testRunner ) );
               break;
         }
      }

      for ( Future<FeatureResult> future : futures )
      {
         future.get();
      }

      result.setEndTime( new Date() );
      return result;
   }

   /**
    * Runs a feature file using the RunInfo provided and returns the FeatureResult
    */
   public FeatureResult runFeatureFile( RunInfo runInfo, String featureFileName )
   {
      try
      {
         String featureSource = ClassPathLoaderUtils.readAllText( featureFileName );

         if ( StringUtils.isEmpty( featureSource ) )
         {
            String errorMsg = "Feature file invalid: " + featureFileName;
            log.error( errorMsg );
            return StandardFeatureResults.error( featureFileName, errorMsg );
         }
         return runFeatureSource( runInfo, featureSource );
      }
      catch ( Throwable t )
      {
         log.error( Constants.EMPTY_STRING, t );
         return StandardFeatureResults.error( featureFileName, t );
      }
   }

   /**
    * Runs the feature source string using the RunInfo provided and returns the FeatureResult
    */
   public FeatureResult runFeatureSource( RunInfo runInfo, String featureFileSourceString )
   {
      try
      {
         FeatureSourceParser featureParser = getFeatureSourceParser( runInfo );

         if ( featureParser == null )
         {
            String errorMsg = "Failed to get a feature source parser of type: " + runInfo.getFeatureSourceParserPlugin();
            log.error( errorMsg );
            return StandardFeatureResults.error( Constants.UNNAMED, errorMsg );
         }
         TestFeature testFeature = featureParser.parseFeatureSource( featureFileSourceString );

         return runFeature( runInfo, testFeature );
      }
      catch ( Throwable t )
      {
         log.error( Constants.EMPTY_STRING, t );
         return StandardFeatureResults.error( Constants.UNNAMED, t );
      }
   }

   /**
    * Runs a TestFeature locally (or remotely invoked) using the RunInfo provided and returns the FeatureResult
    */
   public FeatureResult runFeature( RunInfo runInfo, TestFeature feature ) throws InterruptedException
   {
      StepRunner stepRunner = getStepRunner( runInfo );

      if ( stepRunner == null )
      {
         String errorMsg = "Failed to get a step runner for run: " + runInfo;
         log.error( errorMsg );
         return StandardFeatureResults.error( feature.getName(), errorMsg );
      }

      ArrayList<TestDataSource> testDataSources = getTestDataSources( runInfo );

      if ( testDataSources == null )
      {
         String errorMsg = "Failed to get test data sources for run: " + runInfo;
         log.error( errorMsg );
         return StandardFeatureResults.error( feature.getName(), errorMsg );
      }
      try
      {
         FeatureRunner featureRunner = FeatureRunner.builder().kartaRuntime( this ).runInfo( runInfo ).testFeature( feature ).build();
         return featureRunner.call();
      }
      catch ( InterruptedException ie )
      {
         throw ie;
      }
      catch ( Throwable t )
      {
         log.error( Constants.EMPTY_STRING, t );
         return StandardFeatureResults.error( feature.getName(), t );
      }
   }

   /**
    * Runs a TestJob iteration on remote node or locally
    */
   public TestJobResult runJobIteration( RunInfo runInfo, String featureName, TestJob job, int iterationIndex, BeanRegistry contextBeanRegistry )
            throws Throwable
   {
      TestJobResult jobResult;
      String        node = job.getNode();
      if ( StringUtils.isNotEmpty( node ) )
      {
         KartaNode nodeObj = nodeRegistry.getNode( node );

         if ( nodeObj == null )
         {
            throw new Exception( "Configuration issue: Node with name " + node + " is not registered in node registry" );
         }

         jobResult = nodeObj.runJobIteration( runInfo, featureName, job.toBuilder().node( null ).build(), iterationIndex );

         if ( jobResult == null )
         {
            // ( "Null job result received from remote node" );
            return TestJobResult.builder().successful( false ).error( true ).build();
         }

         jobResult.processRemoteResults();
      }
      else
      {
         jobResult = TestJobRunner.run( this, runInfo, featureName, job, iterationIndex, contextBeanRegistry );
      }

      return jobResult;
   }

   /**
    * Runs a PreparedScenario locally (or remotely invoked) using the RunInfo provided and returns the ScenarioResult
    */
   public ScenarioResult runTestScenario( RunInfo runInfo, String featureName, int iterationIndex, PreparedScenario testScenario,
                                          long scenarioIterationNumber ) throws InterruptedException
   {
      ScenarioRunner scenarioRunner = ScenarioRunner.builder().kartaRuntime( this ).runInfo( runInfo ).featureName( featureName )
               .iterationIndex( iterationIndex ).testScenario( testScenario ).scenarioIterationNumber( scenarioIterationNumber ).build();
      return scenarioRunner.call();
   }

   /**
    * Takes care of post execution of a test step </br>
    * - raises incidents returned from the StepResult </br>
    * - raises other events returned from the StepResult </br>
    * - merges the result map into the variables of the TestExecutionContext </br>
    */
   public void processStepResult( Date startTime, StepResult stepResult, TestExecutionContext testExecutionContext )
   {
      if ( stepResult.getStartTime() == null )
      {
         stepResult.setStartTime( startTime );
      }

      if ( stepResult.getEndTime() == null )
      {
         stepResult.setEndTime( new Date() );
      }

      for ( TestIncident incident : stepResult.getIncidents() )
      {
         eventProcessor.raiseEvent( new TestIncidentOccurrenceEvent( testExecutionContext, incident ) );
      }

      for ( Event event : stepResult.getEvents() )
      {
         eventProcessor.raiseEvent( event );
      }

      DataUtils.mergeMapInto( stepResult.getResults(), testExecutionContext.getVariables() );
   }

   /**
    * Returns merged test data from test step and variable test data from rules
    */
   public HashMap<String, Serializable> getMergedTestData( TestStep step ) throws Throwable
   {
      HashMap<String, Serializable>                  mergedTestData          = DataUtils.cloneMap( step.getTestData() );
      HashMap<String, HashMap<String, Serializable>> variableTestDataRuleMap = step.getVariableTestDataRules();
      if ( variableTestDataRuleMap != null )
      {
         ObjectMapper objectMapper = ParserUtils.getObjectMapper();

         for ( String objectKey : variableTestDataRuleMap.keySet() )
         {
            if ( !mergedTestData.containsKey( objectKey ) )
            {
               ObjectGenerationRule ruleToAdd = objectMapper.readValue( objectMapper.writeValueAsString( variableTestDataRuleMap.get( objectKey ) ),
                        ObjectGenerationRule.class );

               if ( !ruleToAdd.validateConfiguration() )
               {
                  log.error( "Object rule generation failed validation: " + ruleToAdd );
                  continue;
               }

               mergedTestData.put( objectKey, ruleToAdd.generateNextValue( random ) );
            }
         }
      }

      return mergedTestData;
   }

   /**
    * Converts a TestStep into PreparedStep which is ready for execution with execution context and test data merged
    */
   public PreparedStep getPreparedStep( RunInfo runInfo, String featureName, int iterationIndex, String scenarioName,
                                        HashMap<String, Serializable> variables, HashMap<String, ArrayList<Serializable>> commonTestDataSet,
                                        TestStep step, BeanRegistry contextBeanRegistry ) throws Throwable
   {
      StepRunner stepRunner     = getStepRunner( runInfo );
      String     stepIdentifier = step.getStep();

      ArrayList<TestStep> nestedSteps = step.getSteps();

      if ( nestedSteps == null )
      {
         if ( StringUtils.isBlank( stepIdentifier ) )
         {
            log.error( "Empty step definition identifier for step " + step );
         }

         String sanitizedStepIdentifier = stepRunner.sanitizeStepIdentifier( stepIdentifier );
         TestExecutionContext testExecutionContext = new TestExecutionContext( runInfo.getRunName(), featureName, iterationIndex, scenarioName,
                  sanitizedStepIdentifier, null, variables );
         testExecutionContext.setContextBeanRegistry( contextBeanRegistry );

         HashMap<String, Serializable> mergedTestData = getMergedTestData( step );
         testExecutionContext.mergeTestData( mergedTestData, DataUtils.mergeMaps( commonTestDataSet, step.getTestDataSet() ),
                  getTestDataSources( runInfo ) );

         return PreparedStep.builder().identifier( stepIdentifier ).testExecutionContext( testExecutionContext ).node( step.getNode() )
                  .numberOfThreads( step.getNumberOfThreads() ).maxRetries( step.getMaxRetries() ).condition( step.getCondition() ).build();
      }
      else
      {
         HashMap<String, Serializable> mergedTestData = getMergedTestData( step );
         TestExecutionContext testExecutionContext = new TestExecutionContext( runInfo.getRunName(), featureName, iterationIndex, scenarioName,
                  stepIdentifier, null, variables );
         testExecutionContext.mergeTestData( mergedTestData, DataUtils.mergeMaps( commonTestDataSet, step.getTestDataSet() ),
                  getTestDataSources( runInfo ) );
         testExecutionContext.setContextBeanRegistry( contextBeanRegistry );

         PreparedStep preparedStepGroup = PreparedStep.builder().identifier( stepIdentifier ).testExecutionContext( testExecutionContext )
                  .node( step.getNode() ).numberOfThreads( step.getNumberOfThreads() ).maxRetries( step.getMaxRetries() ).build();
         ArrayList<PreparedStep> nestedPreparedSteps = new ArrayList<>();

         for ( TestStep nestedStep : nestedSteps )
         {
            // Pass parent test data set to children.
            nestedPreparedSteps.add( getPreparedStep( runInfo, featureName, iterationIndex, scenarioName, variables,
                     DataUtils.mergeMaps( commonTestDataSet, step.getTestDataSet() ), nestedStep, contextBeanRegistry ) );
         }

         preparedStepGroup.setSteps( nestedPreparedSteps );
         Boolean runInParallel = step.getRunStepsInParallel();
         preparedStepGroup.setRunStepsInParallel( runInParallel != null && runInParallel );
         preparedStepGroup.setCondition( step.getCondition() );
         return preparedStepGroup;
      }
   }

   /**
    * Converts a ChaosAction into PreparedChaosAction which is ready for execution with execution context and test data merged
    */
   public PreparedChaosAction getPreparedChaosAction( RunInfo runInfo, String featureName, int iterationIndex, String scenarioName,
                                                      HashMap<String, Serializable> variables,
                                                      HashMap<String, ArrayList<Serializable>> commonTestDataSet, ChaosAction chaosAction,
                                                      BeanRegistry contextBeanRegistry ) throws Throwable
   {
      TestExecutionContext testExecutionContext = new TestExecutionContext( runInfo.getRunName(), featureName, iterationIndex, scenarioName,
               chaosAction.getName(), null, variables );
      testExecutionContext.setContextBeanRegistry( contextBeanRegistry );
      testExecutionContext.mergeTestData( null, commonTestDataSet, getTestDataSources( runInfo ) );
      return PreparedChaosAction.builder().name( chaosAction.getName() ).node( chaosAction.getNode() ).subjects( chaosAction.getSubjects() )
               .chaos( chaosAction.getChaos() ).testExecutionContext( testExecutionContext ).build();
   }

   /**
    * Converts a TestScenario into PreparedScenario which is ready for execution with execution context and test data merged
    */
   public PreparedScenario getPreparedScenario( RunInfo runInfo, String featureName, int iterationIndex, HashMap<String, Serializable> variables,
                                                HashMap<String, ArrayList<Serializable>> commonTestDataSet, ArrayList<TestStep> scenarioSetupSteps,
                                                TestScenario testScenario, ArrayList<TestStep> scenarioTearDownSteps ) throws Throwable
   {
      BeanRegistry contextBeanRegistry = new BeanRegistry();

      PreparedScenario preparedScenario = PreparedScenario.builder().name( testScenario.getName() ).description( testScenario.getDescription() )
               .contextBeanRegistry( contextBeanRegistry ).build();

      HashMap<String, ArrayList<Serializable>> mergedCommonTestDataSet = DataUtils.mergeMaps( commonTestDataSet, testScenario.getTestDataSet() );

      ArrayList<PreparedStep> preparedSetupSteps = new ArrayList<>();
      for ( TestStep step : DataUtils.mergeLists( scenarioSetupSteps, testScenario.getSetupSteps() ) )
      {
         preparedSetupSteps.add(
                  getPreparedStep( runInfo, featureName, iterationIndex, testScenario.getName(), variables, mergedCommonTestDataSet, step,
                           contextBeanRegistry ) );
      }
      preparedScenario.setSetupSteps( preparedSetupSteps );

      ArrayList<PreparedChaosAction> preparedChaosActions = new ArrayList<>();
      ChaosActionTreeNode            chaosConfiguration   = testScenario.getChaosConfiguration();
      if ( chaosConfiguration != null )
      {
         if ( chaosConfiguration.checkForValidity() )
         {
            ArrayList<ChaosAction> chaosActionsToPerform = chaosConfiguration.nextChaosActions( random );
            // TODO: Handle chaos action being empty

            for ( ChaosAction chaosAction : chaosActionsToPerform )
            {
               preparedChaosActions.add(
                        getPreparedChaosAction( runInfo, featureName, iterationIndex, testScenario.getName(), variables, mergedCommonTestDataSet,
                                 chaosAction, contextBeanRegistry ) );
            }
         }
      }
      preparedScenario.setChaosActions( preparedChaosActions );

      ArrayList<PreparedStep> preparedExecutionSteps = new ArrayList<>();
      for ( TestStep step : testScenario.getExecutionSteps() )
      {
         preparedExecutionSteps.add(
                  getPreparedStep( runInfo, featureName, iterationIndex, testScenario.getName(), variables, mergedCommonTestDataSet, step,
                           contextBeanRegistry ) );
      }
      preparedScenario.setExecutionSteps( preparedExecutionSteps );

      ArrayList<PreparedStep> preparedTearDownSteps = new ArrayList<>();
      for ( TestStep step : DataUtils.mergeLists( testScenario.getTearDownSteps(), scenarioTearDownSteps ) )
      {
         preparedTearDownSteps.add(
                  getPreparedStep( runInfo, featureName, iterationIndex, testScenario.getName(), variables, mergedCommonTestDataSet, step,
                           contextBeanRegistry ) );
      }
      preparedScenario.setTearDownSteps( preparedTearDownSteps );

      return preparedScenario;
   }

   /**
    * Evaluates if the step to run based on condition in the step. True if no condition mentioned.
    */
   public boolean shouldStepNeedNotBeRun( RunInfo runInfo, PreparedStep step ) throws InterruptedException
   {
      String condition = step.getCondition();
      if ( StringUtils.isNotBlank( condition ) )
      {
         StepRunner stepRunner = getStepRunner( runInfo );
         return !stepRunner.runCondition( step.getTestExecutionContext(), condition );
      }
      return false;
   }

   /**
    * Runs a PreparedStep based on the RunInfo locally or on a remote node and returns the StepResult
    */
   public StepResult runStep( RunInfo runInfo, PreparedStep step ) throws Exception
   {
      Date       startTime = new Date();
      StepResult stepResult;

      String node = step.getNode();
      if ( StringUtils.isNotEmpty( node ) )
      {
         KartaNode nodeObj = nodeRegistry.getNode( node );

         if ( nodeObj == null )
         {
            throw new Exception( "Configuration issue: Node with name " + node + " is not registered in node registry" );
         }

         stepResult = nodeObj.runStep( runInfo, step.toBuilder().node( null ).build() );

         if ( stepResult == null )
         {
            return StandardStepResults.error( "Null step result received from remote node" );
         }

         stepResult.processRemoteResults();
      }
      else
      {
         int numberOfThreadsInParallel = step.getNumberOfThreads();

         // TODO: Add max validations
         if ( numberOfThreadsInParallel > 1 )
         {
            stepResult = new StepResult();
            ExecutorService stepExecutorService = new ThreadPoolExecutor( numberOfThreadsInParallel, numberOfThreadsInParallel, 0L,
                     TimeUnit.MILLISECONDS, new BlockingRunnableQueue( numberOfThreadsInParallel ) );

            for ( int i = 0; i < numberOfThreadsInParallel; i++ )
            {
               PreparedStepRunner preparedStepRunner = PreparedStepRunner.builder().kartaRuntime( this ).runInfo( runInfo ).step( step )
                        .resultConsumer( stepResult::mergeResults ).build();
               stepExecutorService.submit( preparedStepRunner );
            }

            stepExecutorService.shutdown();

            if ( !stepExecutorService.awaitTermination( Long.MAX_VALUE, TimeUnit.SECONDS ) )
            {
               log.error( "Wait for executor service termination failed." );
            }

         }
         else
         {
            PreparedStepRunner preparedStepRunner = PreparedStepRunner.builder().kartaRuntime( this ).runInfo( runInfo ).step( step ).build();
            stepResult = preparedStepRunner.call();
         }
      }

      processStepResult( startTime, stepResult, step.getTestExecutionContext() );

      return stepResult;
   }

   /**
    * Runs a TestStep based on the RunInfo locally or on a remote node and returns the StepResult
    */
   public StepResult runStep( RunInfo runInfo, String featureName, int iterationIndex, String scenarioName, HashMap<String, Serializable> variables,
                              HashMap<String, ArrayList<Serializable>> commonTestDataSet, TestStep step, BeanRegistry contextBeanRegistry )
            throws Throwable
   {
      return runStep( runInfo,
               getPreparedStep( runInfo, featureName, iterationIndex, scenarioName, variables, commonTestDataSet, step, contextBeanRegistry ) );
   }

   /**
    * Runs a PreparedChaosAction based on the RunInfo locally or on a remote node and returns the StepResult
    */
   public StepResult runChaosAction( RunInfo runInfo, PreparedChaosAction preparedChaosAction ) throws Exception
   {
      Date       startTime = new Date();
      StepResult stepResult;

      String node = preparedChaosAction.getNode();
      if ( StringUtils.isNotEmpty( node ) )
      {
         KartaNode nodeObj = nodeRegistry.getNode( node );

         if ( nodeObj == null )
         {
            throw new Exception( "Configuration issue: Node with name " + node + " is not registered in node registry" );
         }

         stepResult = nodeObj.performChaosAction( runInfo, preparedChaosAction.toBuilder().node( null ).build() );

         if ( stepResult == null )
         {
            return StandardStepResults.error( "Null step result received from remote node" );
         }

         stepResult.processRemoteResults();
      }
      else
      {
         StepRunner stepRunner = getStepRunner( runInfo );
         stepResult = stepRunner.performChaosAction( preparedChaosAction );
      }

      processStepResult( startTime, stepResult, preparedChaosAction.getTestExecutionContext() );

      return stepResult;
   }

   /**
    * Runs a ChaosAction based on the RunInfo locally or on a remote node and returns the StepResult
    */
   public StepResult runChaosAction( RunInfo runInfo, String featureName, int iterationIndex, String scenarioName,
                                     HashMap<String, Serializable> variables, HashMap<String, ArrayList<Serializable>> commonTestDataSet,
                                     ChaosAction chaosAction, BeanRegistry contextBeanRegistry ) throws Throwable
   {
      return runChaosAction( runInfo,
               getPreparedChaosAction( runInfo, featureName, iterationIndex, scenarioName, variables, commonTestDataSet, chaosAction,
                        contextBeanRegistry ) );
   }
}
