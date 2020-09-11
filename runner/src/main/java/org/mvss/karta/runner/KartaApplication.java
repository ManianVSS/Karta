package org.mvss.karta.runner;

import javax.annotation.PreDestroy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import lombok.extern.log4j.Log4j2;

@Log4j2
@SpringBootApplication
public class KartaApplication
{
   public static void main( String[] args )
   {
      // Spring boot start
      SpringApplication.run( KartaApplication.class, args );
   }

   @PreDestroy
   public void onDestroy() throws Exception
   {
      log.info( "******************** Stopping Karta Server *********************" );
   }

}
