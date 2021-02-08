package org.mvss.karta.framework.runtime.impl;

import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.TestEventListener;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RemoteHTMLTestEventListener implements TestEventListener
{
   private static final String PLUGIN_NAME             = "RemoteHTMLTestEventListener";

   @PropertyMapping( group = PLUGIN_NAME, value = "url" )
   private String              url                     = Constants.HTTPS + Constants.LOCALHOST + Constants.COLON + "18080" + Constants.PATH_REPORT_HTML_EVENT;

   private boolean             initialized             = false;

   private RequestSpecBuilder  requestSpecBuilder;

   @PropertyMapping( group = PLUGIN_NAME, value = "disableCertificateCheck" )
   private boolean             disableCertificateCheck = true;

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
      log.info( "URL is: " + url );
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
   public void processEvent( Event event )
   {
      try
      {
         RestAssured.given( requestSpecBuilder.build() ).body( event ).post();
      }
      catch ( Throwable t )
      {
         log.error( Constants.EMPTY_STRING, t );
      }
   }

   @Override
   public void close()
   {
      log.info( "Closing " + PLUGIN_NAME + " ... " );
   }
}
