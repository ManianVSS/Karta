package org.mvss.karta.framework.runtime.impl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.TestDataSource;
import org.mvss.karta.framework.runtime.models.ExecutionStepPointer;
import org.mvss.karta.framework.utils.ParserUtils;
import org.mvss.karta.framework.utils.PropertyUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DataFilesTestDataSource implements TestDataSource
{
   public static final String                                                                                                     PLUGIN_NAME       = "DataFilesTestDataSource";

   public static final TypeReference<HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<Serializable>>>>>> testDataStoreType = new TypeReference<HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<Serializable>>>>>>()
                                                                                                                                                    {
                                                                                                                                                    };

   private HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<Serializable>>>>>                            dataStore         = new HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<Serializable>>>>>();

   @PropertyMapping( group = PLUGIN_NAME, value = "dataPath" )
   private ArrayList<String>                                                                                                      dataPath          = new ArrayList<String>();

   private boolean                                                                                                                initialized       = false;

   @Override
   public String getPluginName()
   {
      return PLUGIN_NAME;
   }

   public void mergeDataStore( HashMap<String, HashMap<String, HashMap<String, HashMap<String, ArrayList<Serializable>>>>> dataStoreToMerge )
   {
      for ( String featureDataKey : dataStoreToMerge.keySet() )
      {
         if ( !dataStore.containsKey( featureDataKey ) )
         {
            dataStore.put( featureDataKey, new HashMap<String, HashMap<String, HashMap<String, ArrayList<Serializable>>>>() );
         }

         HashMap<String, HashMap<String, HashMap<String, ArrayList<Serializable>>>> featureData = dataStore.get( featureDataKey );
         HashMap<String, HashMap<String, HashMap<String, ArrayList<Serializable>>>> featureDataToMerge = dataStoreToMerge.get( featureDataKey );

         for ( String scenarioDataKey : featureDataToMerge.keySet() )
         {
            if ( !featureData.containsKey( scenarioDataKey ) )
            {
               featureData.put( scenarioDataKey, new HashMap<String, HashMap<String, ArrayList<Serializable>>>() );
            }

            HashMap<String, HashMap<String, ArrayList<Serializable>>> scenarioData = featureData.get( scenarioDataKey );
            HashMap<String, HashMap<String, ArrayList<Serializable>>> scenarioDataToMerge = featureDataToMerge.get( scenarioDataKey );

            for ( String stepDataKey : scenarioDataToMerge.keySet() )
            {
               if ( !scenarioData.containsKey( stepDataKey ) )
               {
                  scenarioData.put( stepDataKey, new HashMap<String, ArrayList<Serializable>>() );
               }

               HashMap<String, ArrayList<Serializable>> stepData = scenarioData.get( stepDataKey );
               HashMap<String, ArrayList<Serializable>> stepDataToMerge = scenarioDataToMerge.get( stepDataKey );

               for ( String testDataKey : stepDataToMerge.keySet() )
               {
                  stepData.put( testDataKey, stepDataToMerge.get( testDataKey ) );
               }
            }

         }
      }
   }

   public void mergeDataStore( File dataFileToMerge ) throws JsonMappingException, JsonProcessingException, IOException
   {
      mergeDataStore( ParserUtils.readValue( ParserUtils.getFileDataFormat( dataFileToMerge.getName() ), FileUtils.readFileToString( dataFileToMerge, Charset.defaultCharset() ), testDataStoreType ) );
   }

   public void mergeDataDirectory( File pathFile ) throws JsonMappingException, JsonProcessingException, IOException
   {
      for ( File dataFile : FileUtils.listFiles( pathFile, Constants.dataFileExtentions, true ) )
      {
         mergeDataStore( dataFile );
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
      PropertyUtils.expandEnvVars( dataPath );

      for ( String dataPathEntry : dataPath )
      {
         File pathFile = new File( dataPathEntry );

         if ( pathFile.exists() )
         {
            if ( pathFile.isDirectory() )
            {
               mergeDataDirectory( pathFile );
            }
            else
            {
               mergeDataStore( pathFile );
            }
         }
      }

      initialized = true;
      return true;
   }

   @Override
   public HashMap<String, Serializable> getData( ExecutionStepPointer executionStepPointer ) throws Throwable
   {
      HashMap<String, Serializable> testData = new HashMap<String, Serializable>();

      if ( executionStepPointer != null )
      {
         try
         {
            String featureName = executionStepPointer.getFeature();

            if ( featureName == null )
            {
               featureName = Constants.GENERIC_FEATURE;
            }

            String scenarioName = executionStepPointer.getScenario();

            if ( scenarioName == null )
            {
               scenarioName = Constants.GENERIC_SCENARIO;
            }

            String stepName = executionStepPointer.getStep();

            if ( stepName == null )
            {
               stepName = Constants.GENERIC_STEP;
            }

            if ( dataStore.containsKey( featureName ) )
            {
               HashMap<String, HashMap<String, HashMap<String, ArrayList<Serializable>>>> featureData = dataStore.get( featureName );

               if ( featureData.containsKey( scenarioName ) )
               {
                  HashMap<String, HashMap<String, ArrayList<Serializable>>> scenarioData = featureData.get( scenarioName );

                  if ( scenarioData.containsKey( stepName ) )
                  {
                     HashMap<String, ArrayList<Serializable>> stepData = scenarioData.get( stepName );

                     for ( String dataKey : stepData.keySet() )
                     {
                        ArrayList<Serializable> possibleValues = stepData.get( dataKey );
                        if ( ( possibleValues != null ) && !possibleValues.isEmpty() )
                        {
                           int iterationIndex = executionStepPointer.getIterationIndex();
                           if ( iterationIndex <= 0 )
                           {
                              iterationIndex = 0;
                           }
                           int valueIndex = iterationIndex % possibleValues.size();
                           testData.put( dataKey, possibleValues.get( valueIndex ) );
                        }
                     }
                  }
               }
            }
         }
         catch ( Throwable t )
         {
            log.error( t );
         }
      }
      return testData;
   }

   @Override
   public void close()
   {
      log.info( "Closing " + PLUGIN_NAME + " ..." );

   }

}
