package org.mvss.karta;

import javax.annotation.PreDestroy;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.mvss.karta.framework.config.RunConfig;
import org.mvss.karta.framework.core.TestRunner;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootApplication
public class KartaApplication implements CommandLineRunner
{
   @Autowired
   RunConfig                   runConfig;

   private static final String KARTA   = "Karta";
   private static final String HELP    = "help";
   private static final String RUNTEST = "runTest";

   public static void main( String[] args )
   {
      SpringApplication.run( KartaApplication.class, args );
   }

   @PreDestroy
   public void onDestroy() throws Exception
   {
      log.info( "******************** Spring Container is destroyed! *********************" );
   }

   @Override
   public void run( String... args ) throws Exception
   {
      Options options = new Options();
      HelpFormatter formatter = new HelpFormatter();

      options.addOption( "r", RUNTEST, false, "run a test class" );
      options.addOption( null, HELP, false, "prints this help message" );

      DefaultParser parser = new DefaultParser();

      try
      {
         CommandLine cmd = parser.parse( options, args );

         if ( cmd.hasOption( HELP ) )
         {

            formatter.printHelp( KARTA, options );
            System.exit( 0 );
         }

         if ( cmd.hasOption( RUNTEST ) )
         {
            TestRunner testRunner = new TestRunner();
            BeanUtils.copyProperties( runConfig, testRunner );

            testRunner.run();
            System.exit( 0 );
         }
      }
      catch ( UnrecognizedOptionException uoe )
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
