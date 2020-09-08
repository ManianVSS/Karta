package org.mvss.karta.runner;

import javax.annotation.PreDestroy;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.mvss.karta.runner.core.JavaTestRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootApplication
public class KartaApplication
{
   private static final String KARTA      = "Karta";
   private static final String HELP       = "help";
   private static final String RUN_TEST   = "runTest";
   private static final String RUN_CLASS  = "runClass";
   private static final String JAR_FILE   = "jarFile";
   private static final String RUN_SERVER = "runServer";

   public static void main( String[] args )
   {
      Options options = new Options();
      HelpFormatter formatter = new HelpFormatter();
      DefaultParser parser = new DefaultParser();

      options.addOption( "r", RUN_TEST, false, "run a test case" );
      options.addOption( "c", RUN_CLASS, true, "test case class to run" );
      options.addOption( "j", JAR_FILE, true, "jar file which contains the test" );
      options.addOption( null, HELP, false, "prints this help message" );
      options.addOption( "s", RUN_SERVER, false, "runs the test server" );

      try
      {
         CommandLine cmd = parser.parse( options, args );

         if ( cmd.hasOption( HELP ) )
         {
            formatter.printHelp( KARTA, options );
            System.exit( 0 );
         }
         else if ( cmd.hasOption( RUN_TEST ) )
         {
            if ( !cmd.hasOption( RUN_CLASS ) )
            {
               formatter.printHelp( KARTA, options );
               System.exit( -1 );
            }

            String className = cmd.getOptionValue( RUN_CLASS );
            String jarFile = cmd.hasOption( JAR_FILE ) ? cmd.getOptionValue( JAR_FILE ) : null;
            JavaTestRunner testRunner = JavaTestRunner.builder().className( className ).jarFile( jarFile ).build();
            testRunner.run();
            System.exit( 0 );
         }
         else if ( cmd.hasOption( RUN_SERVER ) )
         {
            SpringApplication.run( KartaApplication.class, args );
         }
         else
         {
            formatter.printHelp( KARTA, options );
            System.exit( -1 );
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

   @PreDestroy
   public void onDestroy() throws Exception
   {
      log.info( "******************** Stopping Karta *********************" );
   }

}
