package org.mvss.karta.samples.plugins;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CSVTestDataSourcePlugin implements TestDataSource
{
   public static final String  PLUGIN_NAME  = "CSVTestDataSourcePlugin";

   @PropertyMapping( group = PLUGIN_NAME, value = "csvFileName" )
   private String              csvFileName  = "TestData.csv";

   private boolean             initialized  = false;

   private CSVReader           csvReader;

   private String[]            headerRecord;

   private static ObjectMapper objectMapper = ParserUtils.getYamlObjectMapper();

   private Object              writeLock    = new Object();

   @Override
   public String getPluginName()
   {
      return PLUGIN_NAME;
   }

   private void resetCSV() throws Throwable
   {
      synchronized ( writeLock )
      {
         File file = new File( csvFileName );
         FileReader filereader = new FileReader( file );
         csvReader = new CSVReader( filereader );
         headerRecord = csvReader.readNext();

         if ( ( headerRecord == null ) || ( headerRecord.length == 0 ) )
         {
            filereader.close();
            throw new Exception( "CSV file does not have data or headers " + csvFileName );
         }
      }
   }

   @Override
   public boolean initialize() throws Throwable
   {
      if ( initialized )
      {
         return true;
      }

      log.info( "Initializing " + PLUGIN_NAME + " plugin" );

      resetCSV();
      initialized = true;
      return true;
   }

   @Override
   public HashMap<String, Serializable> getData( ExecutionStepPointer executionStepPointer ) throws Throwable
   {
      HashMap<String, Serializable> testData = new HashMap<String, Serializable>();
      try
      {

         // TODO: retrieve record for the execution step pointer instead of cycling

         String[] nextRecord;

         synchronized ( writeLock )
         {
            if ( ( nextRecord = csvReader.readNext() ) == null )
            {
               resetCSV();
               if ( ( nextRecord = csvReader.readNext() ) == null )
               {
                  return testData;
               }
            }
         }

         for ( int i = 0; i < headerRecord.length; i++ )
         {
            testData.put( headerRecord[i], objectMapper.readValue( nextRecord[i], Serializable.class ) );
         }

      }
      catch ( Throwable t )
      {
         log.error( "", t );
      }
      return testData;
   }

   @Override
   public void close()
   {
      log.info( "Closing " + PLUGIN_NAME + " ..." );

      if ( csvReader != null )
      {
         try
         {
            csvReader.close();
            csvReader = null;
         }
         catch ( IOException ioe )
         {
            log.error( ioe );
         }
      }
   }

}
