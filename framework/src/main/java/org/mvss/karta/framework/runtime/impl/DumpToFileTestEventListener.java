package org.mvss.karta.framework.runtime.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.runtime.Configurator;
import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.TestEventListener;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DumpToFileTestEventListener implements TestEventListener
{
   public static final String PLUGIN_NAME  = "KartaDumpToFileTestEventListener";

   private ObjectOutputStream outputStream = null;

   @PropertyMapping( group = PLUGIN_NAME, value = "fileName" )
   private String             fileName     = "KartaEventsRawDump.bin";

   private boolean            initialized  = false;

   @Override
   public String getPluginName()
   {
      return PLUGIN_NAME;
   }

   @Override
   public boolean initialize( HashMap<String, HashMap<String, Serializable>> properties ) throws Throwable
   {
      if ( initialized )
      {
         return true;
      }

      Configurator.loadProperties( properties, this );

      log.debug( "Initializing Yerkin plugin with " + properties );

      File eventDumpFile = new File( fileName );

      if ( !eventDumpFile.exists() )
      {
         eventDumpFile.createNewFile();
      }

      outputStream = new ObjectOutputStream( new FileOutputStream( eventDumpFile, true ) );
      initialized = true;

      return true;
   }

   @Override
   public synchronized void processEvent( Event event )
   {
      try
      {
         outputStream.writeObject( event );
      }
      catch ( IOException e )
      {
         log.error( e );
      }
   }

   @Override
   public void close()
   {
      try
      {
         log.info( "Closing " + PLUGIN_NAME + " ... " );

         if ( outputStream != null )
         {
            outputStream.close();
            outputStream = null;
         }
      }
      catch ( IOException e )
      {
         log.error( e );
      }
   }
}
