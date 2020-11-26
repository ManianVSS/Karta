package org.mvss.karta.samples.plugins;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.runtime.interfaces.FeatureSourceParser;
import org.mvss.karta.framework.utils.DataUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class GherkinPlugin implements FeatureSourceParser
{
   public static final String       PLUGIN_NAME  = "Gherkin";

   public static final List<String> conjunctions = Arrays.asList( "Given ", "When ", "Then ", "And ", "But " );

   private boolean                  initialized  = false;

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

      return true;
   }

   @Override
   public TestFeature parseFeatureSource( String sourceCode ) throws Throwable
   {
      TestFeature feature = new TestFeature();
      String[] lines = sourceCode.split( "\n" );
      int linePointer = 0;

      boolean featureFound = false;

      while ( linePointer < lines.length )
      {
         String line = lines[linePointer++];
         line = line.trim();

         if ( line.startsWith( "Feature:" ) )
         {
            feature.setName( line.substring( "Feature:".length() ).trim() );
            feature.setDescription( Constants.EMPTY_STRING );
            featureFound = true;
            break;
         }
      }

      if ( !featureFound )
      {
         throw new Exception( "Feature source is missing feature declaration " );
      }

      boolean scenarioFound = false;
      boolean isScenarioOutline = false;
      TestScenario testScenario = null;
      ArrayList<TestStep> stepsContainer = null;
      TestStep currentStep = null;
      boolean inExamples = false;
      boolean inDataTable = false;
      ArrayList<String> headerList = null;
      HashMap<String, ArrayList<Serializable>> testData = null;
      boolean inStepScope = false;

      while ( linePointer < lines.length )
      {
         String line = lines[linePointer++];
         line = line.trim();

         if ( line.startsWith( "|" ) && inStepScope )
         {
            inStepScope = false;
            inDataTable = true;
         }

         if ( inDataTable || inExamples )
         {
            if ( line.startsWith( "|" ) )
            {
               if ( headerList == null )
               {
                  headerList = new ArrayList<String>();
                  testData = new HashMap<String, ArrayList<Serializable>>();
                  for ( String item : line.split( "[|]" ) )
                  {
                     String trimmedItem = item.trim();
                     if ( StringUtils.isNotEmpty( trimmedItem ) )
                     {
                        headerList.add( trimmedItem );
                        testData.put( trimmedItem, new ArrayList<Serializable>() );
                     }
                  }
               }
               else
               {
                  int i = 0;
                  for ( String item : line.split( "[|]" ) )
                  {
                     String trimmedItem = item.trim();
                     if ( StringUtils.isNotEmpty( trimmedItem ) )
                     {
                        testData.get( headerList.get( i ) ).add( trimmedItem );
                        i++;
                     }
                  }
               }
               continue;
            }
            else
            {
               if ( inDataTable )
               {
                  if ( currentStep.getTestDataSet() == null )
                  {
                     currentStep.setTestDataSet( new HashMap<String, ArrayList<Serializable>>() );
                  }
                  DataUtils.mergeMapInto( testData, currentStep.getTestDataSet() );
               }
               else
               {
                  if ( testScenario == null )
                  {
                     if ( feature.getTestDataSet() == null )
                     {
                        feature.setTestDataSet( new HashMap<String, ArrayList<Serializable>>() );
                     }
                     DataUtils.mergeMapInto( testData, feature.getTestDataSet() );
                  }
                  else
                  {
                     if ( testScenario.getTestDataSet() == null )
                     {
                        testScenario.setTestDataSet( new HashMap<String, ArrayList<Serializable>>() );
                     }
                     DataUtils.mergeMapInto( testData, testScenario.getTestDataSet() );
                  }
               }

               headerList = null;
               testData = null;
               inExamples = false;
               inDataTable = false;
            }
         }

         if ( StringUtils.isEmpty( line ) )
         {
            isScenarioOutline = false;
            inStepScope = false;
            inExamples = false;
            inDataTable = false;
         }

         if ( line.startsWith( "Scenario:" ) || line.startsWith( "Scenario Outline:" ) )
         {
            inStepScope = false;
            isScenarioOutline = line.startsWith( "Scenario Outline:" );
            testScenario = new TestScenario();
            testScenario.setName( line.substring( ( isScenarioOutline ? "Scenario Outline:" : "Scenario:" ).length() ).trim() );
            testScenario.setDescription( Constants.EMPTY_STRING );
            feature.getTestScenarios().add( testScenario );
            stepsContainer = testScenario.getExecutionSteps();
            scenarioFound = true;
            continue;
         }

         if ( line.startsWith( "Background:" ) )
         {
            inStepScope = false;
            isScenarioOutline = false;
            stepsContainer = feature.getScenarioSetupSteps();
            continue;
         }

         if ( line.startsWith( "#" ) )
         {
            continue;
         }

         if ( line.startsWith( "Examples:" ) )
         {
            inStepScope = false;
            inExamples = true;
            isScenarioOutline = false;
            continue;
         }

         boolean stepFound = false;
         for ( String conjuction : conjunctions )
         {
            if ( line.startsWith( conjuction ) )
            {
               stepFound = true;
               currentStep = TestStep.builder().identifier( line.substring( conjuction.length() ).trim() ).build();
               stepsContainer.add( currentStep );
               inStepScope = true;
               isScenarioOutline = false;
               break;
            }
         }
         if ( !stepFound )
         {
            inStepScope = false;
            isScenarioOutline = false;
            if ( scenarioFound )
            {
               testScenario.setDescription( testScenario.getDescription() + line + "\n" );
            }
            else
            {
               feature.setDescription( feature.getDescription() + line + "\n" );
            }
         }
      }

      return feature;
   }
}
