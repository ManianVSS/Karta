package org.mvss.karta.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.RunResult;
import org.mvss.karta.framework.nodes.KartaNodeServer;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.RunInfo;
import org.mvss.karta.framework.runtime.RunTarget;

import lombok.extern.log4j.Log4j2;

/**
 * The entry point for Karta command line interface.</br>
 * 
 * @author Manian
 */
@Log4j2
public class KartaMain
{

   public static List<Runnable> exitHooks = Collections.synchronizedList( new ArrayList<Runnable>() );

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

      options.addOption( "t", Constants.TAGS, true, "tags to run" );

      options.addOption( "f", Constants.FEATURE_FILE, true, "feature file to run" );

      options.addOption( "j", Constants.JAVA_TEST, true, "test case class to run" );
      options.addOption( Constants.JAVA_TEST_JAR, true, "jar file which contains the test" );

      options.addOption( "r", Constants.RUN_NAME, true, "the name of this test run" );

      options.addOption( Constants.RELEASE, true, "the release of the application under test" );
      options.addOption( Constants.BUILD, true, "the build of the application under test" );

      options.addOption( Constants.NUMBER_OF_ITERATIONS, true, "number of iterations. Applicable only  for feature file/java test" );
      options.addOption( Constants.ITERATION_THREAD_COUNT, true, "number of threads to run iterations in parallel with. Applicable only for feature file/java test" );

      options.addOption( Constants.START_NODE, false, "starts Karta RMI node server" );

      options.addOption( null, Constants.HELP, false, "prints this help message" );

      try
      {
         CommandLine cmd = parser.parse( options, args );

         if ( cmd.hasOption( Constants.HELP ) )
         {
            formatter.printHelp( Constants.KARTA, options );
            System.exit( 0 );
         }
         else if ( cmd.hasOption( Constants.START_NODE ) )
         {
            KartaRuntime.initializeNodes = false;
            try (KartaRuntime kartaRuntime = KartaRuntime.getInstance())
            {
               if ( kartaRuntime == null )
               {
                  log.error( "Karta runtime could not be initialized. Please check the directory and config files" );
                  System.exit( -1 );
               }
               KartaNodeServer kartaRMIServer = new KartaNodeServer( kartaRuntime );
               kartaRMIServer.startServer();
               kartaRuntime.addNodes();
               log.info( "Karta node server started " + kartaRMIServer.getNodeConfig() );
               Thread.currentThread().join();
            }
         }
         else
         {
            boolean optionMissing = true;
            boolean runTargetAvailable = false;

            RunInfo runInfo = new RunInfo();
            RunTarget runTarget = new RunTarget();

            if ( cmd.hasOption( Constants.JAVA_TEST ) )
            {
               optionMissing = false;
               runTarget.setJavaTest( cmd.getOptionValue( Constants.JAVA_TEST ) );
            }

            if ( cmd.hasOption( Constants.JAVA_TEST_JAR ) )
            {
               optionMissing = false;
               runTarget.setJavaTestJarFile( cmd.getOptionValue( Constants.JAVA_TEST_JAR ) );
            }

            if ( cmd.hasOption( Constants.FEATURE_FILE ) )
            {
               optionMissing = false;
               runTarget.setFeatureFile( cmd.getOptionValue( Constants.FEATURE_FILE ) );
            }

            if ( cmd.hasOption( Constants.TAGS ) )
            {
               optionMissing = false;
               HashSet<String> tags = new HashSet<String>();
               for ( String tag : cmd.getOptionValue( Constants.TAGS ).split( Constants.COMMA ) )
               {
                  tags.add( tag );
               }
               runTarget.setRunTags( tags );
            }

            if ( cmd.hasOption( Constants.RUN_NAME ) )
            {
               runInfo.setRunName( cmd.getOptionValue( Constants.RUN_NAME ) );
            }
            else
            {
               runInfo.setRunName( Constants.UNNAMED + Constants.HYPHEN + System.currentTimeMillis() );
            }

            if ( cmd.hasOption( Constants.RELEASE ) )
            {
               runInfo.setRelease( cmd.getOptionValue( Constants.RELEASE ) );
            }

            if ( cmd.hasOption( Constants.BUILD ) )
            {
               runInfo.setBuild( cmd.getOptionValue( Constants.BUILD ) );
            }

            if ( cmd.hasOption( Constants.NUMBER_OF_ITERATIONS ) )
            {
               String numberOfIterationsStr = cmd.getOptionValue( Constants.NUMBER_OF_ITERATIONS );

               if ( !StringUtils.isBlank( numberOfIterationsStr ) )
               {
                  runInfo.setNumberOfIterations( Long.parseLong( numberOfIterationsStr ) );
               }
            }

            if ( cmd.hasOption( Constants.ITERATION_THREAD_COUNT ) )
            {
               String iterationThreadCountStr = cmd.getOptionValue( Constants.ITERATION_THREAD_COUNT );

               if ( !StringUtils.isBlank( iterationThreadCountStr ) )
               {
                  runInfo.setNumberOfIterationsInParallel( Integer.parseInt( iterationThreadCountStr ) );
               }
            }

            Runtime.getRuntime().addShutdownHook( new Thread( () -> jvmExitHook() ) );

            runTargetAvailable = runTargetAvailable || StringUtils.isNotBlank( runTarget.getFeatureFile() );
            runTargetAvailable = runTargetAvailable || StringUtils.isNotBlank( runTarget.getJavaTest() );
            runTargetAvailable = runTargetAvailable || ( runTarget.getRunTags() != null && !runTarget.getRunTags().isEmpty() );

            if ( runTargetAvailable )
            {
               try (KartaRuntime kartaRuntime = KartaRuntime.getInstance())
               {
                  if ( kartaRuntime == null )
                  {
                     log.error( "Karta runtime could not be initialized. Please check the directory and config files" );
                     System.exit( -1 );
                  }
                  RunResult runResult = kartaRuntime.runTestTarget( runInfo, runTarget );
                  System.out.println( "Run results are as follows: " );
                  ConcurrentHashMap<String, FeatureResult> resultMap = runResult.getTestResultMap();
                  for ( Entry<String, FeatureResult> entry : resultMap.entrySet() )
                  {
                     System.out.println( entry.getKey() + Constants.COLON + Constants.SPACE + ( entry.getValue().isPassed() ? Constants.PASS : Constants.FAIL ) );
                  }

                  if ( !runResult.isSuccessful() )
                  {
                     System.exit( 1 );
                  }
               }
            }
            else
            {
               if ( optionMissing )
               {
                  formatter.printHelp( Constants.KARTA, options );
                  System.exit( -1 );
               }
            }
         }
      }
      catch ( UnrecognizedOptionException | MissingArgumentException uoe )
      {
         System.err.println( uoe.getMessage() );
         formatter.printHelp( Constants.KARTA, options );
         System.exit( -1 );
      }
      catch ( Throwable t )
      {
         log.error( "Exception caught while init", t );
         formatter.printHelp( Constants.KARTA, options );
         System.exit( -1 );
      }
   }
}
