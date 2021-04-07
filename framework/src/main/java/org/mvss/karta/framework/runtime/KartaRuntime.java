package org.mvss.karta.framework.runtime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.configuration.KartaConfiguration;
import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.chaos.ChaosActionTreeNode;
import org.mvss.karta.framework.core.ClassMethodConsumer;
import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.Initializer;
import org.mvss.karta.framework.core.KartaBean;
import org.mvss.karta.framework.core.LoadConfiguration;
import org.mvss.karta.framework.core.ObjectMethodConsumer;
import org.mvss.karta.framework.core.PreparedChaosAction;
import org.mvss.karta.framework.core.PreparedScenario;
import org.mvss.karta.framework.core.PreparedStep;
import org.mvss.karta.framework.core.RunResult;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StandardFeatureResults;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestIncident;
import org.mvss.karta.framework.core.TestJob;
import org.mvss.karta.framework.core.TestJobResult;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.nodes.KartaNodeConfiguration;
import org.mvss.karta.framework.nodes.KartaNodeRegistry;
import org.mvss.karta.framework.randomization.ObjectGenerationRule;
import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.event.EventProcessor;
import org.mvss.karta.framework.runtime.event.RunCompleteEvent;
import org.mvss.karta.framework.runtime.event.RunStartEvent;
import org.mvss.karta.framework.runtime.event.TestIncidentOccurrenceEvent;
import org.mvss.karta.framework.runtime.interfaces.FeatureSourceParser;
import org.mvss.karta.framework.runtime.interfaces.Plugin;
import org.mvss.karta.framework.runtime.interfaces.StepRunner;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.interfaces.TestEventListener;
import org.mvss.karta.framework.runtime.interfaces.TestLifeCycleHook;
import org.mvss.karta.framework.runtime.testcatalog.Test;
import org.mvss.karta.framework.runtime.testcatalog.TestCatalogManager;
import org.mvss.karta.framework.runtime.testcatalog.TestCategory;
import org.mvss.karta.framework.threading.BlockingRunnableQueue;
import org.mvss.karta.framework.utils.AnnotationScanner;
import org.mvss.karta.framework.utils.ClassPathLoaderUtils;
import org.mvss.karta.framework.utils.DataUtils;
import org.mvss.karta.framework.utils.DynamicClassLoader;
import org.mvss.karta.framework.utils.ParserUtils;
import org.mvss.karta.framework.utils.PropertyUtils;
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
   private KartaNodeRegistry      nodeRegistry;

   private static ObjectMapper    yamlObjectMapper = ParserUtils.getYamlObjectMapper();

   @Getter
   private BeanRegistry           beanRegistry;

   @Getter
   private ExecutorServiceManager executorServiceManager;

   /**
    * This flag is used to run a custom Karta Minion server by disabling node initializing first and call addNodes later
    */
   public static boolean          initializeNodes  = true;

   @Getter
   private RunInfo                defaultRunInfo   = null;

   /**
    * Default constructor is reserved for default runtime instance. Use getInstance.
    * 
    * @throws JsonMappingException
    * @throws JsonProcessingException
    * @throws IOException
    * @throws URISyntaxException
    */
   private KartaRuntime() throws JsonMappingException, JsonProcessingException, IOException, URISyntaxException
   {

   }

   private static KartaRuntime instance        = null;

   private static Object       _syncLockObject = new Object();

   /**
    * Gets the default KartaRuntime singleton instance.
    * 
    * @return KartaRuntime
    * @throws Throwable
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
    * 
    * @return boolean
    * @throws JsonMappingException
    * @throws JsonProcessingException
    * @throws IOException
    * @throws URISyntaxException
    * @throws IllegalArgumentException
    * @throws IllegalAccessException
    * @throws NotBoundException
    * @throws ClassNotFoundException
    * @throws InvocationTargetException
    */
   public boolean initializeRuntime() throws JsonMappingException, JsonProcessingException, IOException, URISyntaxException, IllegalArgumentException, IllegalAccessException, NotBoundException, ClassNotFoundException, InvocationTargetException
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
            File kartaHomeConfigFile = Paths.get( PropertyUtils.systemPropertyMap.get( Constants.KARTA_HOME ), Constants.BIN, Constants.KARTA_CONFIGURATION_YAML ).toFile();
            if ( kartaHomeConfigFile.exists() )
            {
               configString = FileUtils.readFileToString( kartaHomeConfigFile, Charset.defaultCharset() );

               if ( configString == null )
               {
                  return false;
               }
            }
            else
            {
               return false;
            }
         }
      }

      kartaConfiguration = yamlObjectMapper.readValue( configString, KartaConfiguration.class );

      configString = ClassPathLoaderUtils.readAllText( Constants.KARTA_CONFIGURATION_OVERRIDES_YAML );
      if ( configString != null )
      {
         KartaConfiguration kartaOverrideConfiguration = yamlObjectMapper.readValue( configString, KartaConfiguration.class );
         kartaConfiguration.overrideConfiguration( kartaOverrideConfiguration );
      }

      kartaConfiguration.expandSystemAndEnvProperties();

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
         propertiesFileList = new ArrayList<String>();
      }

      if ( !propertiesFileList.isEmpty() )
      {
         propertiesFileList.add( Constants.KARTA_RUNTIME_PROPERTIES_YAML );
      }

      String[] propertyFilesToLoad = new String[propertiesFileList.size()];
      propertiesFileList.toArray( propertyFilesToLoad );
      configurator.mergePropertiesFiles( propertyFilesToLoad );

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
      pnpRegistry.getEnabledPluginsOfType( TestEventListener.class ).forEach( ( plugin ) -> eventProcessor.addEventListener( (TestEventListener) plugin ) );
      pnpRegistry.getEnabledPluginsOfType( TestLifeCycleHook.class ).forEach( ( plugin ) -> eventProcessor.addLifeCycleHook( (TestLifeCycleHook) plugin ) );

      /*---------------------------------------------------------------------------------------------------------------------*/
      // Initialize node registry
      /*---------------------------------------------------------------------------------------------------------------------*/
      // TODO: Add task pulling worker minions to support minions as clients rather than open server sockets
      nodeRegistry = new KartaNodeRegistry();

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
            catalogFileText = ClassPathLoaderUtils.readAllText( testCatalogFragmentFile );
            testCategory = ( catalogFileText == null ) ? new TestCategory() : yamlObjectMapper.readValue( catalogFileText, TestCategory.class );
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

      defaultRunInfo = RunInfo.builder().runName( Constants.UNNAMED ).featureSourceParserPlugin( kartaConfiguration.getDefaultFeatureSourceParser() ).stepRunnerPluginName( kartaConfiguration.getDefaultStepRunner() )
               .testDataSourcePlugins( kartaConfiguration.getDefaultTestDataSources() ).build();

      return true;
   }

   private static List<Class<?>>    configuredBeanClasses           = Collections.synchronizedList( new ArrayList<Class<?>>() );

   private final Consumer<Method>   processBeanDefinition           = new Consumer<Method>()
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

                                                                                String beanName = kartaBean.value();

                                                                                Class<?>[] paramTypes = beanDefinitionMethod.getParameterTypes();

                                                                                Object beanObj = null;

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

   private final Consumer<Class<?>> processLoadPropertiesDefinition = new Consumer<Class<?>>()
                                                                    {
                                                                       @Override
                                                                       public void accept( Class<?> classesToLoadPropertiesWith )
                                                                       {
                                                                          try
                                                                          {
                                                                             initializeClass( classesToLoadPropertiesWith );
                                                                          }
                                                                          catch ( Throwable t )
                                                                          {
                                                                             log.error( "Exception while loading static fileds from properties for class  " + classesToLoadPropertiesWith.getName(), t );
                                                                          }

                                                                       }
                                                                    };

   public void processConfigBeans( Collection<String> configurationScanPackageNames )
   {
      AnnotationScanner.forEachMethod( configurationScanPackageNames, KartaBean.class, AnnotationScanner.IS_PUBLIC_AND_STATIC, AnnotationScanner.IS_NON_VOID_RETURN_TYPE, AnnotationScanner.DOES_NOT_HAVE_PARAMETERS, processBeanDefinition );
      AnnotationScanner.forEachClass( configurationScanPackageNames, LoadConfiguration.class, AnnotationScanner.IS_PUBLIC, processLoadPropertiesDefinition );
   }

   private final ObjectMethodConsumer callObjectInitializer = new ObjectMethodConsumer()
   {
      @Override
      public void accept( Object object, Method method )
      {
         try
         {
            method.invoke( object );
         }
         catch ( Throwable t )
         {
            log.error( "Exception while parsing bean definition from method  " + method.getName(), t );
         }
      }
   };

   /**
    * Sets the properties and beans(static and non-static) for the object and calls any initializer methods
    * 
    * @see Initializer
    * @param object
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
         AnnotationScanner.forEachMethod( object, Initializer.class, AnnotationScanner.IS_NON_STATIC, null, AnnotationScanner.DOES_NOT_HAVE_PARAMETERS, callObjectInitializer );
      }
      catch ( Throwable t )
      {
         log.error( "Exception while initializing object", t );
      }
   }

   private final ClassMethodConsumer callClassInitializer = new ClassMethodConsumer()
   {
      @Override
      public void accept( Class<?> classToWorkWith, Method beanDefinitionMethod )
      {
         try
         {
            beanDefinitionMethod.invoke( null );
         }
         catch ( Throwable t )
         {
            log.error( "Exception while parsing bean definition from method  " + beanDefinitionMethod.getName(), t );
         }

      }
   };

   /**
    * Sets the properties and beans (static) for the class of object and calls any initializer methods
    * 
    * @see Initializer
    * @param theClassOfObject
    */
   public void initializeClass( Class<?> theClassOfObject )
   {
      try
      {
         if ( !configuredBeanClasses.contains( theClassOfObject ) )
         {
            configurator.loadProperties( theClassOfObject );
            beanRegistry.loadStaticBeans( theClassOfObject );

            AnnotationScanner.forEachMethod( theClassOfObject, Initializer.class, AnnotationScanner.IS_STATIC, null, AnnotationScanner.DOES_NOT_HAVE_PARAMETERS, callClassInitializer );
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
   public void addNodes()
   {
      for ( KartaNodeConfiguration node : kartaConfiguration.getNodes() )
      {
         nodeRegistry.addNode( node );
      }
   }

   /**
    * Auto-closable method for the runtime. Stops all tests and Karta components.
    */
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

   private static HashMap<String, FeatureSourceParser>                featureSourceParserMap = new HashMap<String, FeatureSourceParser>();
   private static HashMap<String, StepRunner>                         stepRunnerMap          = new HashMap<String, StepRunner>();
   private static HashMap<HashSet<String>, ArrayList<TestDataSource>> testDataSourcesMap     = new HashMap<HashSet<String>, ArrayList<TestDataSource>>();

   private Object                                                     fspMapLock             = new Object();
   private Object                                                     srMapLock              = new Object();
   private Object                                                     tdsMapLock             = new Object();

   /**
    * Returns the FeatureSourceParser based on the feature source parser plugin name provided.
    * 
    * @param featureSourceParserName
    * @return FeatureSourceParser
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
    * 
    * @param runInfo
    * @return FeatureSourceParser
    */
   public FeatureSourceParser getFeatureSourceParser( RunInfo runInfo )
   {
      String featureSourceParserName = runInfo.getFeatureSourceParserPlugin();
      return getFeatureSourceParser( featureSourceParserName );
   }

   /**
    * Returns the StepRunner based on the step runner plugin name provided.
    * 
    * @param stepRunnerPluginName
    * @return StepRunner
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
    * 
    * @param runInfo
    * @return StepRunner
    */
   public StepRunner getStepRunner( RunInfo runInfo )
   {
      String stepRunnerPluginName = runInfo.getStepRunnerPluginName();
      return getStepRunner( stepRunnerPluginName );
   }

   /**
    * Returns a list of TestDataSources based on the set of test data source plugin names provided
    * 
    * @param testDataSourcesPluginNames
    * @return ArrayList&lt;TestDataSource&gt;
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
               ArrayList<TestDataSource> testDataSources = new ArrayList<TestDataSource>();

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
    * 
    * @param runInfo
    * @return ArrayList&lt;TestDataSource&gt;
    */
   public ArrayList<TestDataSource> getTestDataSources( RunInfo runInfo )
   {
      HashSet<String> testDataSourcesPluginNames = runInfo.getTestDataSourcePlugins();
      return getTestDataSources( testDataSourcesPluginNames );
   }

   /**
    * Runs a RunTarget and returns if the feature/JavaTestCase or Tags passed
    * 
    * @param runInfo
    * @param runTarget
    * @return boolean
    */
   public RunResult runTestTarget( RunInfo runInfo, RunTarget runTarget )
   {
      RunResult runResult = new RunResult();

      runInfo.setDefaultPlugins( kartaConfiguration.getDefaultFeatureSourceParser(), kartaConfiguration.getDefaultStepRunner(), kartaConfiguration.getDefaultTestDataSources() );

      try
      {
         String runName = runInfo.getRunName();

         HashSet<String> individualTestTags = new HashSet<String>();
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

            JavaFeatureRunner testRunner = JavaFeatureRunner.builder().kartaRuntime( this ).runInfo( runInfo ).javaTest( runTarget.getJavaTest() ).javaTestJarFile( runTarget.getJavaTestJarFile() ).build();
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
    * 
    * @param runInfo
    * @param tags
    * @return boolean
    * @throws Throwable
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
    * 
    * @param runInfo
    * @param tests
    * @return boolean
    * @throws Throwable
    */
   public RunResult runTest( RunInfo runInfo, Collection<Test> tests ) throws Throwable
   {
      RunResult result = new RunResult();
      ArrayList<Future<FeatureResult>> futures = new ArrayList<Future<FeatureResult>>();

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

               FeatureRunner featureRunner = FeatureRunner.builder().kartaRuntime( this ).runInfo( runInfoForTest ).testFeature( testFeature ).resultConsumer( ( featureResult ) -> result.addTestResult( featureResult ) ).build();

               futures.add( testExecutorService.submit( featureRunner ) );
               break;

            case JAVA_TEST:
               JavaFeatureRunner testRunner = JavaFeatureRunner.builder().kartaRuntime( this ).runInfo( runInfo ).javaTest( test.getJavaTestClass() ).javaTestJarFile( test.getSourceArchive() )
                        .resultConsumer( ( featureResult ) -> result.addTestResult( featureResult ) ).build();
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
    * 
    * @param runInfo
    * @param featureFileName
    * @return FeatureResult
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
    * 
    * @param runInfo
    * @param featureFileSourceString
    * @return FeatureResult
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
    * 
    * @param runInfo
    * @param feature
    * @return FeatureResult
    */
   public FeatureResult runFeature( RunInfo runInfo, TestFeature feature )
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
         FeatureResult featureResult = featureRunner.call();
         return featureResult;
      }
      catch ( Throwable t )
      {
         log.error( Constants.EMPTY_STRING, t );
         return StandardFeatureResults.error( feature.getName(), t );
      }
   }

   /**
    * Runs a TestJob iteration on remote node or locally
    * 
    * @param runInfo
    * @param featureName
    * @param job
    * @param iterationIndex
    * @param contextBeanRegistry
    * @return TestJobResult
    * @throws Throwable
    */
   public TestJobResult runJobIteration( RunInfo runInfo, String featureName, TestJob job, int iterationIndex, BeanRegistry contextBeanRegistry ) throws Throwable
   {
      TestJobResult jobResult = null;
      String node = job.getNode();
      if ( StringUtils.isNotEmpty( node ) )
      {
         // TODO: Handle local node
         // TODO: Handle null node error
         jobResult = nodeRegistry.getNode( node ).runJobIteration( runInfo, featureName, job.toBuilder().node( null ).build(), iterationIndex );
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
    * 
    * @param runInfo
    * @param featureName
    * @param iterationIndex
    * @param testScenario
    * @param scenarioIterationNumber
    * @return ScenarioResult
    */
   public ScenarioResult runTestScenario( RunInfo runInfo, String featureName, int iterationIndex, PreparedScenario testScenario, long scenarioIterationNumber )
   {
      ScenarioRunner scenarioRunner = ScenarioRunner.builder().kartaRuntime( this ).runInfo( runInfo ).featureName( featureName ).iterationIndex( iterationIndex ).testScenario( testScenario ).scenarioIterationNumber( scenarioIterationNumber ).build();
      return scenarioRunner.call();
   }

   /**
    * Takes care of post execution of a test step </br>
    * - raises incidents returned in the StepResult </br>
    * - raises other events returned in the StepResult </br>
    * - merges the result map into the variables of the TestExecutionContext </br>
    * 
    * @param startTime
    * @param stepResult
    * @param testExecutionContext
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
    * 
    * @param step
    * @return
    * @throws Throwable
    */
   public HashMap<String, Serializable> getMergedTestData( TestStep step ) throws Throwable
   {
      HashMap<String, Serializable> mergedTestData = DataUtils.cloneMap( step.getTestData() );
      HashMap<String, HashMap<String, Serializable>> variableTestDataRuleMap = step.getVariableTestDataRules();
      if ( variableTestDataRuleMap != null )
      {
         ObjectMapper objectMapper = ParserUtils.getObjectMapper();

         for ( String objectKey : variableTestDataRuleMap.keySet() )
         {
            if ( !mergedTestData.containsKey( objectKey ) )
            {
               ObjectGenerationRule ruleToAdd = objectMapper.readValue( objectMapper.writeValueAsString( variableTestDataRuleMap.get( objectKey ) ), ObjectGenerationRule.class );

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
    * 
    * @param runInfo
    * @param featureName
    * @param iterationIndex
    * @param scenarioName
    * @param variables
    * @param commonTestDataSet
    * @param step
    * @return PreparedStep
    * @throws Throwable
    */
   public PreparedStep getPreparedStep( RunInfo runInfo, String featureName, int iterationIndex, String scenarioName, HashMap<String, Serializable> variables, HashMap<String, ArrayList<Serializable>> commonTestDataSet, TestStep step,
                                        BeanRegistry contextBeanRegistry )
            throws Throwable
   {
      StepRunner stepRunner = getStepRunner( runInfo );
      String stepIdentifier = step.getStep();

      ArrayList<TestStep> nestedSteps = step.getSteps();

      if ( nestedSteps == null )
      {
         if ( StringUtils.isBlank( stepIdentifier ) )
         {
            log.error( "Empty step definition identifier for step " + step );
         }

         String sanitizedStepIdentifer = stepRunner.sanitizeStepIdentifier( stepIdentifier );
         TestExecutionContext testExecutionContext = new TestExecutionContext( runInfo.getRunName(), featureName, iterationIndex, scenarioName, sanitizedStepIdentifer, null, variables );
         testExecutionContext.setContextBeanRegistry( contextBeanRegistry );

         HashMap<String, Serializable> mergedTestData = getMergedTestData( step );
         testExecutionContext.mergeTestData( mergedTestData, DataUtils.mergeMaps( commonTestDataSet, step.getTestDataSet() ), getTestDataSources( runInfo ) );

         return PreparedStep.builder().identifier( stepIdentifier ).testExecutionContext( testExecutionContext ).node( step.getNode() ).numberOfThreads( step.getNumberOfThreads() ).maxRetries( step.getMaxRetries() ).condition( step.getCondition() )
                  .build();
      }
      else
      {
         HashMap<String, Serializable> mergedTestData = getMergedTestData( step );
         TestExecutionContext testExecutionContext = new TestExecutionContext( runInfo.getRunName(), featureName, iterationIndex, scenarioName, stepIdentifier, null, variables );
         testExecutionContext.mergeTestData( mergedTestData, DataUtils.mergeMaps( commonTestDataSet, step.getTestDataSet() ), getTestDataSources( runInfo ) );
         testExecutionContext.setContextBeanRegistry( contextBeanRegistry );

         PreparedStep preparedStepGroup = PreparedStep.builder().identifier( stepIdentifier ).testExecutionContext( testExecutionContext ).node( step.getNode() ).numberOfThreads( step.getNumberOfThreads() ).maxRetries( step.getMaxRetries() ).build();
         ArrayList<PreparedStep> nestedPreparedSteps = new ArrayList<PreparedStep>();

         for ( TestStep nestedStep : nestedSteps )
         {
            nestedPreparedSteps.add( getPreparedStep( runInfo, featureName, iterationIndex, scenarioName, variables, commonTestDataSet, nestedStep, contextBeanRegistry ) );
         }

         preparedStepGroup.setSteps( nestedPreparedSteps );
         Boolean runInParallel = step.getRunStepsInParallel();
         preparedStepGroup.setRunStepsInParallel( runInParallel == null ? false : runInParallel );
         preparedStepGroup.setCondition( step.getCondition() );
         return preparedStepGroup;
      }
   }

   /**
    * Converts a ChaosAction into PreparedChaosAction which is ready for execution with execution context and test data merged
    * 
    * @param runInfo
    * @param featureName
    * @param iterationIndex
    * @param scenarioName
    * @param variables
    * @param commonTestDataSet
    * @param chaosAction
    * @param contextBeanRegistry
    * @return PreparedChaosAction
    * @throws Throwable
    */
   public PreparedChaosAction getPreparedChaosAction( RunInfo runInfo, String featureName, int iterationIndex, String scenarioName, HashMap<String, Serializable> variables, HashMap<String, ArrayList<Serializable>> commonTestDataSet,
                                                      ChaosAction chaosAction, BeanRegistry contextBeanRegistry )
            throws Throwable
   {
      TestExecutionContext testExecutionContext = new TestExecutionContext( runInfo.getRunName(), featureName, iterationIndex, scenarioName, chaosAction.getName(), null, variables );
      testExecutionContext.setContextBeanRegistry( contextBeanRegistry );
      testExecutionContext.mergeTestData( null, commonTestDataSet, getTestDataSources( runInfo ) );
      return PreparedChaosAction.builder().name( chaosAction.getName() ).node( chaosAction.getNode() ).subjects( chaosAction.getSubjects() ).chaos( chaosAction.getChaos() ).testExecutionContext( testExecutionContext ).build();
   }

   /**
    * Converts a TestScenario into PreparedScenario which is ready for execution with execution context and test data merged
    * 
    * @param runInfo
    * @param featureName
    * @param iterationIndex
    * @param variables
    * @param commonTestDataSet
    * @param scenarioSetupSteps
    * @param testScenario
    * @param scenarioTearDownSteps
    * @return PreparedScenario
    * @throws Throwable
    */
   public PreparedScenario getPreparedScenario( RunInfo runInfo, String featureName, int iterationIndex, HashMap<String, Serializable> variables, HashMap<String, ArrayList<Serializable>> commonTestDataSet, ArrayList<TestStep> scenarioSetupSteps,
                                                TestScenario testScenario, ArrayList<TestStep> scenarioTearDownSteps )
            throws Throwable
   {
      BeanRegistry contextBeanRegistry = new BeanRegistry();

      PreparedScenario preparedScenario = PreparedScenario.builder().name( testScenario.getName() ).description( testScenario.getDescription() ).contextBeanRegistry( contextBeanRegistry ).build();

      HashMap<String, ArrayList<Serializable>> mergedCommonTestDataSet = DataUtils.mergeMaps( commonTestDataSet, testScenario.getTestDataSet() );

      ArrayList<PreparedStep> preparedSetupSteps = new ArrayList<PreparedStep>();
      for ( TestStep step : DataUtils.mergeLists( scenarioSetupSteps, testScenario.getSetupSteps() ) )
      {
         preparedSetupSteps.add( getPreparedStep( runInfo, featureName, iterationIndex, testScenario.getName(), variables, mergedCommonTestDataSet, step, contextBeanRegistry ) );
      }
      preparedScenario.setSetupSteps( preparedSetupSteps );

      ArrayList<PreparedChaosAction> preparedChaosActions = new ArrayList<PreparedChaosAction>();
      ChaosActionTreeNode chaosConfiguration = testScenario.getChaosConfiguration();
      if ( chaosConfiguration != null )
      {
         if ( chaosConfiguration.checkForValidity() )
         {
            ArrayList<ChaosAction> chaosActionsToPerform = chaosConfiguration.nextChaosActions( random );
            // TODO: Handle chaos action being empty

            for ( ChaosAction chaosAction : chaosActionsToPerform )
            {
               preparedChaosActions.add( getPreparedChaosAction( runInfo, featureName, iterationIndex, testScenario.getName(), variables, mergedCommonTestDataSet, chaosAction, contextBeanRegistry ) );
            }
         }
      }
      preparedScenario.setChaosActions( preparedChaosActions );

      ArrayList<PreparedStep> preparedExecutionSteps = new ArrayList<PreparedStep>();
      for ( TestStep step : testScenario.getExecutionSteps() )
      {
         preparedExecutionSteps.add( getPreparedStep( runInfo, featureName, iterationIndex, testScenario.getName(), variables, mergedCommonTestDataSet, step, contextBeanRegistry ) );
      }
      preparedScenario.setExecutionSteps( preparedExecutionSteps );

      ArrayList<PreparedStep> preparedTearDownSteps = new ArrayList<PreparedStep>();
      for ( TestStep step : DataUtils.mergeLists( testScenario.getTearDownSteps(), scenarioTearDownSteps ) )
      {
         preparedTearDownSteps.add( getPreparedStep( runInfo, featureName, iterationIndex, testScenario.getName(), variables, mergedCommonTestDataSet, step, contextBeanRegistry ) );
      }
      preparedScenario.setTearDownSteps( preparedTearDownSteps );

      return preparedScenario;
   }

   /**
    * Evaluates if the step to run based on condition in the step. True if no condition mentioned.
    * 
    * @param runInfo
    * @param step
    * @return
    */
   public boolean shouldStepBeRun( RunInfo runInfo, PreparedStep step )
   {
      String condition = step.getCondition();
      if ( StringUtils.isNotBlank( condition ) )
      {
         StepRunner stepRunner = getStepRunner( runInfo );
         return stepRunner.runCondition( step.getTestExecutionContext(), condition );
      }
      return true;
   }

   /**
    * Runs a PreparedStep based on the RunInfo locally or on a remote node and returns the StepResult
    * 
    * @param runInfo
    * @param step
    * @return StepResult
    * @throws TestFailureException
    * @throws RemoteException
    */
   public StepResult runStep( RunInfo runInfo, PreparedStep step ) throws TestFailureException, RemoteException
   {
      Date startTime = new Date();
      StepResult stepResult;

      String node = step.getNode();
      if ( StringUtils.isNotEmpty( node ) )
      {
         // TODO: Handle local node
         // TODO: Handle null node error
         stepResult = nodeRegistry.getNode( node ).runStep( runInfo, step.toBuilder().node( null ).build() );
         stepResult.processRemoteResults();
      }
      else
      {
         int numberOfThreadsInParallel = step.getNumberOfThreads();

         // TODO: Add max validations
         if ( numberOfThreadsInParallel > 1 )
         {
            stepResult = new StepResult();
            ExecutorService stepExecutorService = new ThreadPoolExecutor( numberOfThreadsInParallel, numberOfThreadsInParallel, 0L, TimeUnit.MILLISECONDS, new BlockingRunnableQueue( numberOfThreadsInParallel ) );

            for ( int i = 0; i < numberOfThreadsInParallel; i++ )
            {
               PreparedStepRunner preparedStepRunner = PreparedStepRunner.builder().kartaRuntime( this ).runInfo( runInfo ).step( step ).resultConsumer( ( threadStepResult ) -> stepResult.mergeResults( threadStepResult ) ).build();
               stepExecutorService.submit( preparedStepRunner );
            }

            stepExecutorService.shutdown();
            try
            {
               // TODO: Change to WaitUtil implementation with max timeout
               stepExecutorService.awaitTermination( Long.MAX_VALUE, TimeUnit.SECONDS );
            }
            catch ( InterruptedException ie )
            {
               if ( !stepExecutorService.isTerminated() )
               {
                  log.warn( "Wait for parallel step threads was interrupted ", ie );
               }
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
    *
    * @param runInfo
    * @param featureName
    * @param iterationIndex
    * @param scenarioName
    * @param variables
    * @param commonTestDataSet
    * @param step
    * @param contextBeanRegistry
    * @return StepResult
    * @throws Throwable
    */
   public StepResult runStep( RunInfo runInfo, String featureName, int iterationIndex, String scenarioName, HashMap<String, Serializable> variables, HashMap<String, ArrayList<Serializable>> commonTestDataSet, TestStep step,
                              BeanRegistry contextBeanRegistry )
            throws Throwable
   {
      return runStep( runInfo, getPreparedStep( runInfo, featureName, iterationIndex, scenarioName, variables, commonTestDataSet, step, contextBeanRegistry ) );
   }

   /**
    * Runs a PreparedChaosAction based on the RunInfo locally or on a remote node and returns the StepResult
    * 
    * @param runInfo
    * @param preparedChaosAction
    * @return StepResult
    * @throws TestFailureException
    * @throws RemoteException
    */
   public StepResult runChaosAction( RunInfo runInfo, PreparedChaosAction preparedChaosAction ) throws TestFailureException, RemoteException
   {
      Date startTime = new Date();
      StepResult stepResult;

      String nodeName = preparedChaosAction.getNode();
      if ( StringUtils.isNotEmpty( nodeName ) )
      {
         stepResult = nodeRegistry.getNode( nodeName ).performChaosAction( runInfo, preparedChaosAction.toBuilder().node( null ).build() );
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
    * 
    * @param runInfo
    * @param featureName
    * @param iterationIndex
    * @param scenarioName
    * @param variables
    * @param commonTestDataSet
    * @param chaosAction
    * @param contextBeanRegistry
    * @return StepResult
    * @throws Throwable
    */
   public StepResult runChaosAction( RunInfo runInfo, String featureName, int iterationIndex, String scenarioName, HashMap<String, Serializable> variables, HashMap<String, ArrayList<Serializable>> commonTestDataSet, ChaosAction chaosAction,
                                     BeanRegistry contextBeanRegistry )
            throws Throwable
   {
      return runChaosAction( runInfo, getPreparedChaosAction( runInfo, featureName, iterationIndex, scenarioName, variables, commonTestDataSet, chaosAction, contextBeanRegistry ) );
   }
}
