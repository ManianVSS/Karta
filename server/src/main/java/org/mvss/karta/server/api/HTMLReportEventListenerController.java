package org.mvss.karta.server.api;

import java.lang.reflect.InvocationTargetException;

import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.event.StandardEventsTypes;
import org.mvss.karta.framework.runtime.impl.HTMLReportTestEventListener;
import org.mvss.karta.framework.runtime.interfaces.Plugin;
import org.mvss.karta.framework.runtime.interfaces.TestEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
public class HTMLReportEventListenerController
{
   @Autowired
   private KartaRuntime                kartaRuntime;

   private HTMLReportTestEventListener htmlReportTestEventListener = null;

   @EventListener( ApplicationReadyEvent.class )
   public void applicationStartup()
   {
      try
      {
         if ( kartaRuntime != null )
         {
            for ( Plugin plugin : kartaRuntime.getPnpRegistry().getPluginsOfType( TestEventListener.class ) )
            {
               if ( plugin.getClass() == HTMLReportTestEventListener.class )
               {
                  htmlReportTestEventListener = (HTMLReportTestEventListener) plugin;
                  break;
               }
            }
         }

         if ( htmlReportTestEventListener == null )
         {
            htmlReportTestEventListener = new HTMLReportTestEventListener();
            kartaRuntime.getConfigurator().loadProperties( htmlReportTestEventListener );
            htmlReportTestEventListener.initialize();
         }
      }
      catch ( Throwable e )
      {
         log.error( "Exception intializing HTML report test event listener: ", e );
      }
   }

   @ResponseStatus( HttpStatus.OK )
   @RequestMapping( method = RequestMethod.POST, value = Constants.PATH_REPORT_HTML_EVENT )
   public void processRunStarted( @RequestBody Event event ) throws IllegalAccessException, InvocationTargetException
   {
      if ( htmlReportTestEventListener != null )
      {
         htmlReportTestEventListener.processEvent( StandardEventsTypes.castToAppropriateEvent( event ) );
      }
   }
}
