package org.mvss.karta.framework.runtime.impl;

import java.util.concurrent.ConcurrentHashMap;

import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.event.StandardEventsTypes;
import org.mvss.karta.framework.runtime.event.dto.Run;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.TestEventListener;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class KartaDashBoardTestEventListener implements TestEventListener
{
   public static final String             PLUGIN_NAME             = "KartaDashBoardTestEventListener";

   @PropertyMapping( group = PLUGIN_NAME, value = "url" )
   private String                         url                     = Constants.HTTPS + Constants.LOCALHOST + Constants.COLON + "18080";

   private RequestSpecBuilder             requestSpecBuilder;

   @PropertyMapping( group = PLUGIN_NAME, value = "disableCertificateCheck" )
   private boolean                        disableCertificateCheck = true;

   private boolean                        initialized             = false;

   private ConcurrentHashMap<String, Run> runMap                  = new ConcurrentHashMap<String, Run>();

   @Override
   public String getPluginName()
   {
      return PLUGIN_NAME;
   }

   @Override
   public boolean initialize() throws Throwable
   {
      if ( initialized )
      {
         return true;
      }
      log.info( "Initializing " + PLUGIN_NAME + " plugin" );
      requestSpecBuilder = new RequestSpecBuilder();
      requestSpecBuilder.setBaseUri( url );

      requestSpecBuilder.addHeader( Constants.CONTENT_TYPE, Constants.APPLICATION_JSON );
      requestSpecBuilder.addHeader( Constants.ACCEPT, Constants.APPLICATION_JSON );

      if ( disableCertificateCheck )
      {
         requestSpecBuilder.setRelaxedHTTPSValidation();
      }

      initialized = true;
      return true;
   }

   @Override
   public synchronized void processEvent( Event event )
   {
      String runName = event.getRunName();
      switch ( event.getEventType() )
      {
         case StandardEventsTypes.RUN_START_EVENT:
            if ( !runMap.containsKey( runName ) )
            {
               Run newRun = Run.builder().name( runName ).build();

               Response response = RestAssured.given( requestSpecBuilder.build() ).param( "name", runName ).body( newRun ).get( Constants.PATH_API_RUNS + "ByName" );

               if ( response.getStatusCode() < 400 )
               {
                  Run existingRun = response.as( Run.class );

                  if ( existingRun != null )
                  {
                     newRun = existingRun;
                  }
               }

               if ( newRun.getId() == null )
               {
                  response = RestAssured.given( requestSpecBuilder.build() ).body( newRun ).post( Constants.PATH_API_RUNS );

                  if ( response.getStatusCode() < 400 )
                  {
                     newRun = response.as( Run.class );
                  }
               }

               runMap.put( runName, newRun );
            }
            break;
      }
   }
}
