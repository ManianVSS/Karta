package org.mvss.karta.server;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.cli.KartaMain;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Log4j2
@SpringBootApplication
public class KartaApplication implements CommandLineRunner
{
   @Autowired
   KartaRuntime kartaRuntime;

   public static void main( String[] args )
   {
      if ( ( args != null ) && args.length > 0 )
      {
         if ( !KartaMain.run( args ) )
         {
            System.exit( 0 );
         }
         else
         {
            System.exit( -1 );
         }
      }
      else
      {
         // Spring boot start
         SpringApplication.run( KartaApplication.class, args );
      }
   }

   @EventListener( ApplicationReadyEvent.class )
   public void applicationStartup()
   {
      kartaRuntime.addNodes();
   }

   public static List<Runnable> exitHooks = Collections.synchronizedList( new ArrayList<>() );

   @Override
   public void run( String... args )
   {
      log.info( "******************** Starting Karta Server *********************" );
   }

   @PreDestroy
   public void onDestroy()
   {
      log.info( "******************** Stopping Karta Server *********************" );

      log.info( "Triggering registered exit hooks" );
      for ( Runnable exitHook : new ArrayList<>( exitHooks ) )
      {
         new Thread( exitHook ).start();
      }
   }

}
