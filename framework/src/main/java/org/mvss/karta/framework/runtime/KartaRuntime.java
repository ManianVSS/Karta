package org.mvss.karta.framework.runtime;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.configuration.KartaBaseConfiguration;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.runtime.event.EventProcessor;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class KartaRuntime
{
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

   private static ObjectMapper       objectMapper = ParserUtils.getObjectMapper();

   public void initializeRuntime() throws JsonMappingException, JsonProcessingException, IOException, URISyntaxException, IllegalArgumentException, IllegalAccessException
   {
      ObjectMapper objectMapper = ParserUtils.getObjectMapper();

      // if ( kartaConfiguration == null )
      // {
      kartaBaseConfiguration = objectMapper.readValue( ClassPathLoaderUtils.readAllText( Constants.KARTA_BASE_CONFIG_JSON ), KartaBaseConfiguration.class );
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

      String pluginsDirectory = kartaBaseConfiguration.getPluginsDirectory();

      if ( StringUtils.isNotEmpty( pluginsDirectory ) )
      {
         pnpRegistry.loadPlugins( configurator, new File( pluginsDirectory ) );
      }

      // if ( runtimeConfiguration == null )
      // {
      kartaRuntimeConfiguration = objectMapper.readValue( ClassPathLoaderUtils.readAllText( Constants.KARTA_RUNTIME_CONFIGURATION_JSON ), KartaRuntimeConfiguration.class );
      // }

      HashSet<String> propertiesFileList = kartaRuntimeConfiguration.getPropertyFiles();
      if ( ( propertiesFileList != null ) && !propertiesFileList.isEmpty() )
      {
         String[] propertyFilesToLoad = new String[propertiesFileList.size()];
         propertiesFileList.toArray( propertyFilesToLoad );
         configurator.mergePropertiesFiles( propertyFilesToLoad );
      }

      pnpRegistry.initializePlugins( configurator.getPropertiesStore() );

      eventProcessor = new EventProcessor();

      pnpRegistry.getPluginsOfType( TestEventListener.class ).forEach( ( plugin ) -> eventProcessor.addEventListener( (TestEventListener) plugin ) );

      configurator.loadProperties( eventProcessor );
      eventProcessor.start();

      testCatalogManager = new TestCatalogManager();

      HashSet<String> testCatalogFiles = kartaRuntimeConfiguration.getTestCatalogFiles();

      if ( ( testCatalogFiles != null ) && !testCatalogFiles.isEmpty() )
      {
         for ( String testCatalogFile : testCatalogFiles )
         {
            TestCategory testCategory = objectMapper.readValue( ClassPathLoaderUtils.readAllText( testCatalogFile ), TestCategory.class );
            testCatalogManager.mergeWithCatalog( testCategory );
         }
      }

      HashSet<String> repoDirNames = kartaRuntimeConfiguration.getTestRepositorydirectories();

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
   }

   public void stopRuntime()
   {
      if ( eventProcessor != null )
      {
         eventProcessor.stop();
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
            instance.initializeRuntime();
         }
      }

      return instance;
   }

   public static HashMap<String, Serializable> getMergedTestData( ArrayList<TestDataSource> testDataSources, ExecutionStepPointer executionStepPointer ) throws Throwable
   {
      HashMap<String, Serializable> mergedTestData = new HashMap<String, Serializable>();

      for ( TestDataSource tds : testDataSources )
      {
         HashMap<String, Serializable> testData = tds.getData( executionStepPointer );
         testData.forEach( ( key, value ) -> mergedTestData.put( key, value ) );
      }

      return mergedTestData;
   }

   public boolean runFeatureFile( String runName, String featureFileName )
   {
      try
      {
         return runFeatureSource( runName, ClassPathLoaderUtils.readAllText( featureFileName ) );
      }
      catch ( Throwable t )
      {
         log.error( t );
         return false;
      }
   }

   public boolean runFeatureSource( String runName, String featureFileSourceString )
   {
      try
      {
         FeatureSourceParser featureParser = (FeatureSourceParser) pnpRegistry.getPlugin( kartaRuntimeConfiguration.getDefaultFeatureSourceParserPlugin(), FeatureSourceParser.class );

         if ( featureParser == null )
         {
            log.error( "Failed to get a feature source parser of type: " + kartaRuntimeConfiguration.getDefaultFeatureSourceParserPlugin() );
            return false;
         }
         TestFeature testFeature = featureParser.parseFeatureSource( featureFileSourceString );

         return run( runName, testFeature );
      }
      catch ( Throwable t )
      {
         log.error( t );
         return false;
      }
   }

   public boolean run( String runName, TestFeature feature )
   {
      StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( kartaRuntimeConfiguration.getDefaultStepRunnerPlugin(), StepRunner.class );

      if ( stepRunner == null )
      {
         log.error( "Failed to get a step runner of type: " + kartaRuntimeConfiguration.getDefaultStepRunnerPlugin() );
         return false;
      }

      ArrayList<TestDataSource> testDataSources = new ArrayList<TestDataSource>();

      for ( String testDataSourcePlugin : kartaRuntimeConfiguration.getDefaultTestDataSourcePlugins() )
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
         FeatureRunner featureRunner = FeatureRunner.builder().stepRunner( stepRunner ).testDataSources( testDataSources ).testProperties( configurator.getPropertiesStore() ).eventProcessor( eventProcessor ).build();
         // configurator.loadProperties( featureRunner );
         return featureRunner.run( runName, feature );
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
      if ( StringUtils.isNotBlank( runTarget.getFeatureFile() ) )
      {
         return runFeatureFile( runName, runTarget.getFeatureFile() );
      }
      else if ( StringUtils.isNotBlank( runTarget.getJavaTest() ) )
      {
         JavaTestRunner testRunner = objectMapper.convertValue( kartaRuntimeConfiguration, JavaTestRunner.class );
         testRunner.setTestProperties( configurator.getPropertiesStore() );
         return testRunner.run( runTarget.getJavaTest(), runTarget.getJavaTestJarFile() );
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
         ArrayList<Test> tests = testCatalogManager.filterTestsByTag( tags );

         for ( Test test : tests )
         {
            switch ( test.getTestType() )
            {
               case FEATURE:
                  FeatureSourceParser featureParser = (FeatureSourceParser) pnpRegistry.getPlugin( test.getFeatureSourceParserPlugin(), FeatureSourceParser.class );

                  if ( featureParser == null )
                  {
                     log.error( "Failed to get a feature source parser of type: " + test.getFeatureSourceParserPlugin() );
                     return false;
                  }
                  // TODO: Handle io errors
                  TestFeature testFeature = featureParser.parseFeatureSource( IOUtils.toString( DynamicClassLoader.getClassPathResourceInJarAsStream( test.getSourceArchive(), test.getFeatureFileName() ), Charset.defaultCharset() ) );

                  StepRunner stepRunner = (StepRunner) pnpRegistry.getPlugin( test.getStepRunnerPlugin(), StepRunner.class );

                  if ( stepRunner == null )
                  {
                     log.error( "Failed to get a step runner of type: " + test.getStepRunnerPlugin() );
                     return false;
                  }

                  ArrayList<TestDataSource> testDataSources = new ArrayList<TestDataSource>();

                  for ( String testDataSourcePlugin : test.getTestDataSourcePlugins() )
                  {
                     TestDataSource testDataSource = (TestDataSource) pnpRegistry.getPlugin( testDataSourcePlugin, TestDataSource.class );

                     if ( testDataSource == null )
                     {
                        log.error( "Failed to get a test data source of type: " + testDataSourcePlugin );
                        return false;
                     }

                     testDataSources.add( testDataSource );
                  }

                  FeatureRunner featureRunner = FeatureRunner.builder().stepRunner( stepRunner ).testDataSources( testDataSources ).testProperties( configurator.getPropertiesStore() ).eventProcessor( eventProcessor ).build();
                  // configurator.loadProperties( featureRunner );
                  featureRunner.run( runName, testFeature, test.getNumberOfIterations(), test.getNumberOfThreads() );
                  break;

               case JAVA_TEST:
                  JavaTestRunner testRunner = JavaTestRunner.builder().testProperties( configurator.getPropertiesStore() ).build();
                  // configurator.loadProperties( testRunner );
                  testRunner.run( test.getJavaTestClass(), test.getSourceArchive() );
                  break;
            }
         }
      }
      catch ( Throwable t )
      {
         log.error( t );
         return false;
      }
      return true;
   }
}
