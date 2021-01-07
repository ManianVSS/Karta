package org.mvss.karta.framework.runtime.impl;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.event.FeatureCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureCompleteEvent;
import org.mvss.karta.framework.runtime.event.StandardEventsTypes;
import org.mvss.karta.framework.runtime.interfaces.PropertyMapping;
import org.mvss.karta.framework.runtime.interfaces.TestEventListener;
import org.mvss.karta.framework.utils.ParserUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class HTMLReportTestEventListener implements TestEventListener
{
   public static final String                        PLUGIN_NAME              = "HTMLReportTestEventListener";

   public static final String                        SPACE                    = " ";
   public static final String                        RUN                      = "Run:";
   public static final String                        FEATURE                  = "Feature:";
   public static final String                        SCENARIO                 = "Scenario:";
   public static final String                        STEP                     = "Step:";
   public static final String                        SETUPSTEP                = "SetupStep:";
   public static final String                        TEARDOWNSTEP             = "TearDownStep:";
   public static final String                        STARTED                  = "started";
   public static final String                        FAILED                   = "failed";
   public static final String                        PASSED                   = "passed";
   public static final String                        COMPLETED                = "completed";

   @PropertyMapping( group = PLUGIN_NAME )
   private String                                    runReportsBaseFolderName = "reports";

   private File                                      runReportsBaseFolder     = null;
   private HashMap<String, HashMap<String, Boolean>> featureResultMap         = new HashMap<String, HashMap<String, Boolean>>();

   private boolean                                   initialized              = false;

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

      runReportsBaseFolder = new File( runReportsBaseFolderName );

      if ( !runReportsBaseFolder.exists() )
      {
         runReportsBaseFolder.mkdirs();
      }
      else
      {
         if ( !runReportsBaseFolder.isDirectory() )
         {
            log.error( runReportsBaseFolderName + " is not a directory" );
            return false;
         }
      }

      initialized = true;
      return true;
   }

   private synchronized HashMap<String, Boolean> getOrCreateFeatureMap( String runName )
   {
      HashMap<String, Boolean> runFeatureMap = featureResultMap.get( runName );

      if ( runFeatureMap == null )
      {
         runFeatureMap = new HashMap<String, Boolean>();
         featureResultMap.put( runName, runFeatureMap );
      }
      return runFeatureMap;
   }

   // TODO: Add more details to report like iteration details and links between test-> feature->scenario->iteration->step report
   @Override
   public synchronized void processEvent( Event event )
   {
      String runName = event.getRunName();

      if ( StringUtils.isEmpty( runName ) )
      {
         return;
      }
      switch ( event.getEventType() )
      {
         case StandardEventsTypes.RUN_START_EVENT:
            Path path = Paths.get( runReportsBaseFolder.getPath(), runName );
            File runDirectory = path.toFile();
            runDirectory.mkdirs();
            getOrCreateFeatureMap( runName );
            break;

         case StandardEventsTypes.RUN_COMPLETE_EVENT:
            HashMap<String, Boolean> runReport = getOrCreateFeatureMap( runName );

            path = Paths.get( runReportsBaseFolder.getPath(), runName, "index.html" );
            File runReportFile = path.toFile();

            try
            {
               StringBuilder runReportBuilder = new StringBuilder();
               runReportBuilder.append( "<!DOCTYPE html>\r\n" + "<html>\r\n" + "<head>\r\n" + "  <title>" + runName + "</title>\r\n" + "<style>\r\n" + "table, th, td {\r\n" + "  border: 1px solid black;\r\n" + "  border-collapse: collapse;\r\n" + "}\r\n"
                                        + "th, td {\r\n" + "  padding: 5px;\r\n" + "}\r\n" + "th {\r\n" + "  text-align: left;\r\n" + "}</style></head>\r\n" + "<body><table>\r\n" + "  <tr>\r\n" + "    <th bgcolor=\"blue\">Feature</th>\r\n"
                                        + "    <th bgcolor=\"blue\">Status</th>\r\n" + "  </tr>" );

               ArrayList<String> keySet = new ArrayList<String>();
               keySet.addAll( runReport.keySet() );
               Collections.sort( keySet );

               for ( String testName : keySet )
               {
                  boolean passed = runReport.get( testName );
                  String tdText = "<td bgcolor=\"" + ( passed ? "green" : "red" ) + "\">";
                  runReportBuilder.append( "  <tr>\r\n" + "    " + tdText + testName + "</td>\r\n" + "    " + tdText + ( passed ? "PASS" : "FAIL" ) + "</td>\r\n" + "  </tr>" );
               }
               runReportBuilder.append( "</table>\r\n" + "</body>\r\n" + "</html>" );
               FileUtils.write( runReportFile, runReportBuilder.toString(), Charset.defaultCharset() );

            }
            catch ( Throwable e )
            {
               log.error( "", e );
            }
            break;

         case StandardEventsTypes.FEATURE_COMPLETE_EVENT:
         case StandardEventsTypes.JAVA_FEATURE_COMPLETE_EVENT:
            FeatureResult featureResult = null;
            String featureName = null;

            if ( event instanceof FeatureCompleteEvent )
            {
               FeatureCompleteEvent featureCompleteEvent = (FeatureCompleteEvent) event;
               TestFeature feature = featureCompleteEvent.getFeature();
               featureName = feature.getName();
               featureResult = featureCompleteEvent.getResult();
            }
            else if ( event instanceof JavaFeatureCompleteEvent )
            {
               JavaFeatureCompleteEvent featureCompleteEvent = (JavaFeatureCompleteEvent) event;
               TestFeature feature = featureCompleteEvent.getFeature();
               featureName = feature.getName();
               featureResult = featureCompleteEvent.getResult();
            }
            else
            {
               break;
            }
            path = Paths.get( runReportsBaseFolder.getPath(), runName, featureName + ".html" );
            Path jsonPath = Paths.get( runReportsBaseFolder.getPath(), runName, featureName + ".json" );

            File featureReportFile = path.toFile();
            File featureJSONDumpFile = jsonPath.toFile();

            // featureDirectory.mkdirs();
            HashMap<String, Boolean> featureMap = getOrCreateFeatureMap( runName );
            featureMap.put( featureName, featureResult.isPassed() );
            try
            {
               featureResult.sortResults();
               StringBuilder featureReportBuilder = new StringBuilder();
               featureReportBuilder.append( "<!DOCTYPE html>\r\n" + "<html>\r\n" + "<head>\r\n" + "  <title>" + runName + "</title>\r\n" + "<style>\r\n" + "table, th, td {\r\n" + "  border: 1px solid black;\r\n" + "  border-collapse: collapse;\r\n"
                                            + "}\r\n" + "th, td {\r\n" + "  padding: 5px;\r\n" + "}\r\n" + "th {\r\n" + "  text-align: left;\r\n" + "}</style></head>\r\n<body>" );

               HashMap<String, ArrayList<ScenarioResult>> scenarioResults = featureResult.getScenarioResultsMap();
               ArrayList<String> keySet = new ArrayList<String>();
               keySet.addAll( scenarioResults.keySet() );
               Collections.sort( keySet );

               // for ( Entry<String, ArrayList<ScenarioResult>> runResultEntrySet : scenarioResults.entrySet() )
               for ( String scenarioName : keySet )
               {
                  ArrayList<ScenarioResult> scenarioResult = scenarioResults.get( scenarioName );

                  featureReportBuilder.append( "<h3>" + scenarioName + "</h3>\r\n<table>\r\n" + "  <tr>\r\n" + "    <th bgcolor=\"blue\">IterationIndex</th>\r\n" + "    <th bgcolor=\"blue\">Status</th>\r\n" + "  </tr>" );

                  for ( ScenarioResult scenarioIterationResult : scenarioResult )
                  {
                     boolean passed = scenarioIterationResult.isPassed();
                     String tdText = "<td bgcolor=\"" + ( passed ? "green" : "red" ) + "\">";
                     featureReportBuilder.append( "  <tr>\r\n" + "    " + tdText + ( scenarioIterationResult.getIterationIndex() + 1 ) + "</td>\r\n" + "    " + tdText + ( passed ? "PASS" : "FAIL" ) + "</td>\r\n" + "  </tr>" );
                  }
                  featureReportBuilder.append( "</table>\r\n" );
               }

               featureReportBuilder.append( "</body>\r\n" + "</html>" );
               FileUtils.write( featureReportFile, featureReportBuilder.toString(), Charset.defaultCharset() );
               FileUtils.write( featureJSONDumpFile, ParserUtils.getObjectMapper().writeValueAsString( featureResult ), Charset.defaultCharset() );
            }
            catch ( Throwable e )
            {
               log.error( "", e );
            }
            break;
      }
   }
}
