package org.mvss.karta.samples.utils;

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
import org.mvss.karta.framework.utils.DataUtils;

public class GherkinUtils
{
   public static final List<String> conjunctions = Arrays.asList( "Given ", "When ", "Then ", "And ", "But " );

   public static TestFeature parseFeatureSource( String sourceCode, ArrayList<String> featureTagList ) throws Throwable
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
         else
         {
            if ( featureTagList != null )
            {
               for ( String word : line.split( Constants.REGEX_WHITESPACE ) )
               {
                  word = word.trim();

                  if ( StringUtils.isBlank( word ) )
                  {
                     continue;
                  }

                  if ( word.startsWith( "@" ) && ( word.length() > 1 ) )
                  {
                     featureTagList.add( word.substring( 1 ).trim() );
                  }
               }
            }
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
               currentStep = TestStep.builder().step( line.substring( conjuction.length() ).trim() ).build();
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
               testScenario.setDescription( ( testScenario.getDescription() + line + "\n" ).trim() );
            }
            else
            {
               feature.setDescription( ( feature.getDescription() + line + "\n" ).trim() );
            }
         }
      }

      return feature;
   }
}
