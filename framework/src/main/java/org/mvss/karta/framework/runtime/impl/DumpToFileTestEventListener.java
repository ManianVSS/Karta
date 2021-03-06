package org.mvss.karta.framework.runtime.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.mvss.karta.framework.core.Initializer;
import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.TestEventListener;
import org.mvss.karta.framework.utils.PropertyUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DumpToFileTestEventListener implements TestEventListener
{
   public static final String PLUGIN_NAME  = "DumpToFileTestEventListener";

   private ObjectOutputStream outputStream = null;

   @PropertyMapping( group = PLUGIN_NAME, value = "fileName" )
   private String             fileName     = "KartaEventsRawDump.bin";

   private boolean            initialized  = false;

   private Object             writeLock    = new Object();

   @Override
   public String getPluginName()
   {
      return PLUGIN_NAME;
   }

   @Initializer
   public boolean initialize() throws Throwable
   {
      if ( initialized )
      {
         return true;
      }

      log.info( "Initializing " + PLUGIN_NAME + " plugin" );

      fileName = PropertyUtils.expandEnvVars( fileName );

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
         synchronized ( writeLock )
         {
            outputStream.writeObject( event );
         }
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
            synchronized ( writeLock )
            {

               outputStream.close();
               outputStream = null;
            }
         }
      }
      catch ( IOException e )
      {
         log.error( e );
      }
   }
}
