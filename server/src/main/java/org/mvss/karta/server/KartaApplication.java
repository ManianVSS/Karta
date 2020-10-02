package org.mvss.karta.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PreDestroy;

import org.mvss.karta.framework.runtime.KartaRuntime;
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
   KartaRuntime kartaRuntime;

   public static void main( String[] args )
   {
      // Spring boot start
      SpringApplication.run( KartaApplication.class, args );
   }

   public static List<Runnable> exitHooks = Collections.synchronizedList( new ArrayList<Runnable>() );

   @Override
   public void run( String... args ) throws Exception
   {
      log.info( "******************** Starting Karta Server *********************" );
      exitHooks.add( () -> kartaRuntime.stopRuntime() );
   }

   @PreDestroy
   public void onDestroy() throws Exception
   {
      log.info( "******************** Stopping Karta Server *********************" );

      log.info( "Triggering registered exit hooks" );
      for ( Runnable exitHook : new ArrayList<Runnable>( exitHooks ) )
      {
         new Thread( exitHook ).run();
      }
   }

}
