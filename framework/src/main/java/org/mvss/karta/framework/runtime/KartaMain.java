package org.mvss.karta.framework.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class KartaMain
{
   private static final String  KARTA                     = "Karta";

   private static final String  HELP                      = "help";

   private static final String  FEATURE_SOURCE_PARSER     = "featureSourceParser";
   private static final String  FEATURE_SOURCE_PARSER_JAR = "featureSourceParserJar";

   private static final String  STEP_RUNNER               = "stepRunner";
   private static final String  STEP_RUNNER_JAR           = "stepRunnerJar";

   private static final String  TEST_DATA_SOURCE          = "testDataSource";
   private static final String  TEST_DATA_SOURCE_JAR      = "testDataSourceJar";

   private static final String  FEATURE_FILE              = "featureFile";

   private static final String  JAVA_TEST                 = "javaTest";
   private static final String  JAVA_TEST_JAR             = "javaTestJar";

   private static final String  CONFIGURATION_FILE        = "configFile";

   public static List<Runnable> exitHooks                 = Collections.synchronizedList( new ArrayList<Runnable>() );

   private static void jvmExitHook()
   {
      log.info( "******************** Stopping Karta  *********************" );
      List<Runnable> exitHooks = new ArrayList<Runnable>( KartaMain.exitHooks );

      log.info( "Triggering registered exit hooks" );
      for ( Runnable exitHook : exitHooks )
      {
         new Thread( exitHook ).run();
      }
   }

   public static void main( String[] args )
   {
      Options options = new Options();
      HelpFormatter formatter = new HelpFormatter();
      DefaultParser parser = new DefaultParser();

      RunConfiguration runConfiguration = new RunConfiguration();

      // options.addOption( "r", RUN_TEST, false, "run a test case" );

      options.addOption( FEATURE_SOURCE_PARSER, true, "feature source parser class name" );
      options.addOption( FEATURE_SOURCE_PARSER_JAR, true, "feature source parser jar file path" );

      options.addOption( STEP_RUNNER, true, "step runner class name" );
      options.addOption( STEP_RUNNER_JAR, true, "step runner jar file path" );

      options.addOption( TEST_DATA_SOURCE, true, "test data source class name" );
      options.addOption( TEST_DATA_SOURCE_JAR, true, "test data source jar file path" );

      options.addOption( "f", FEATURE_FILE, true, "feature file to run" );

      options.addOption( "j", JAVA_TEST, true, "test case class to run" );
      options.addOption( JAVA_TEST_JAR, true, "jar file which contains the test" );

      options.addOption( "c", CONFIGURATION_FILE, true, "test configuration file" );

      options.addOption( null, HELP, false, "prints this help message" );

      try
      {
         CommandLine cmd = parser.parse( options, args );

         if ( cmd.hasOption( HELP ) )
         {
            formatter.printHelp( KARTA, options );
            System.exit( 0 );
         }
         else
         {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );

            boolean optionMissing = true;
            boolean runFeatureFile = false;
            boolean runJavaTest = false;

            if ( cmd.hasOption( CONFIGURATION_FILE ) )
            {
               optionMissing = false;
               runConfiguration = objectMapper.readValue( new File( cmd.getOptionValue( CONFIGURATION_FILE ) ), RunConfiguration.class );
            }

            if ( cmd.hasOption( JAVA_TEST ) )
            {
               optionMissing = false;
               runConfiguration.setJavaTest( cmd.getOptionValue( JAVA_TEST ) );
            }

            if ( cmd.hasOption( JAVA_TEST_JAR ) )
            {
               optionMissing = false;
               runConfiguration.setJavaTestJarFile( cmd.getOptionValue( JAVA_TEST_JAR ) );
            }

            if ( cmd.hasOption( FEATURE_FILE ) )
            {
               optionMissing = false;
               runConfiguration.setFeatureFile( cmd.getOptionValue( FEATURE_FILE ) );
            }

            if ( cmd.hasOption( FEATURE_SOURCE_PARSER ) )
            {
               optionMissing = false;
               runConfiguration.setFeatureSourceParser( cmd.getOptionValue( FEATURE_SOURCE_PARSER ) );
            }

            if ( cmd.hasOption( FEATURE_SOURCE_PARSER_JAR ) )
            {
               optionMissing = false;
               runConfiguration.setFeatureSourceParserJarFile( cmd.getOptionValue( FEATURE_SOURCE_PARSER_JAR ) );
            }

            if ( cmd.hasOption( STEP_RUNNER ) )
            {
               optionMissing = false;
               runConfiguration.setStepRunner( cmd.getOptionValue( STEP_RUNNER ) );
            }

            if ( cmd.hasOption( STEP_RUNNER_JAR ) )
            {
               optionMissing = false;
               runConfiguration.setStepRunnerJarFile( cmd.getOptionValue( STEP_RUNNER_JAR ) );
            }

            if ( cmd.hasOption( TEST_DATA_SOURCE ) )
            {
               optionMissing = false;
               runConfiguration.setTestDataSource( cmd.getOptionValue( TEST_DATA_SOURCE ) );
            }

            if ( cmd.hasOption( TEST_DATA_SOURCE_JAR ) )
            {
               optionMissing = false;
               runConfiguration.setTestDataSourceJarFile( cmd.getOptionValue( TEST_DATA_SOURCE_JAR ) );
            }

            runFeatureFile = StringUtils.isNotBlank( runConfiguration.getFeatureFile() );
            runJavaTest = StringUtils.isNotBlank( runConfiguration.getJavaTest() );

            if ( !( runJavaTest || runFeatureFile ) || optionMissing )
            {
               formatter.printHelp( KARTA, options );
               System.exit( -1 );
            }
            else
            {
               Runtime.getRuntime().addShutdownHook( new Thread( () -> jvmExitHook() ) );

               if ( runFeatureFile )
               {
                  FeatureRunner featureRunner = objectMapper.convertValue( runConfiguration, FeatureRunner.class );
                  featureRunner.run();
               }
               else if ( runJavaTest )
               {
                  JavaTestRunner testRunner = objectMapper.convertValue( runConfiguration, JavaTestRunner.class );;
                  testRunner.run();
               }
               else
               {
                  // Something wrong
                  formatter.printHelp( KARTA, options );
                  System.exit( -1 );
               }
            }
         }
      }
      catch ( UnrecognizedOptionException | MissingArgumentException uoe )
      {
         System.err.println( uoe.getMessage() );
         formatter.printHelp( KARTA, options );
         System.exit( -1 );
      }
      catch ( Throwable t )
      {
         log.error( "Exception caught while init", t );
         formatter.printHelp( KARTA, options );
         System.exit( -1 );
      }
   }
}
