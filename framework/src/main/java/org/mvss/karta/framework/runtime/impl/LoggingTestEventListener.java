package org.mvss.karta.framework.runtime.impl;

import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.ScenarioResult;
import org.mvss.karta.framework.core.StepResult;
import org.mvss.karta.framework.runtime.event.ChaosActionJobCompleteEvent;
import org.mvss.karta.framework.runtime.event.ChaosActionJobStartEvent;
import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.event.FeatureCompleteEvent;
import org.mvss.karta.framework.runtime.event.FeatureSetupStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.FeatureSetupStepStartEvent;
import org.mvss.karta.framework.runtime.event.FeatureStartEvent;
import org.mvss.karta.framework.runtime.event.FeatureTearDownStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.FeatureTearDownStepStartEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureSetupCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureSetupStartEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureStartEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureTearDownCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaFeatureTearDownStartEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioChaosActionCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioChaosActionStartEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioSetupCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioSetupStartEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioStartEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioTearDownCompleteEvent;
import org.mvss.karta.framework.runtime.event.JavaScenarioTearDownStartEvent;
import org.mvss.karta.framework.runtime.event.JobStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.JobStepStartEvent;
import org.mvss.karta.framework.runtime.event.ScenarioChaosActionCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioChaosActionStartEvent;
import org.mvss.karta.framework.runtime.event.ScenarioCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioSetupStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioSetupStepStartEvent;
import org.mvss.karta.framework.runtime.event.ScenarioStartEvent;
import org.mvss.karta.framework.runtime.event.ScenarioStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioStepStartEvent;
import org.mvss.karta.framework.runtime.event.ScenarioTearDownStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.ScenarioTearDownStepStartEvent;
import org.mvss.karta.framework.runtime.event.StandardEventsTypes;
import org.mvss.karta.framework.runtime.event.TestIncidentOccurrenceEvent;
import org.mvss.karta.framework.runtime.interfaces.TestEventListener;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class LoggingTestEventListener implements TestEventListener
{
   public static final String PLUGIN_NAME  = "LoggingTestEventListener";

   public static final String SPACE        = " ";
   public static final String RUN          = "Run:";
   public static final String FEATURE      = "Feature:";
   public static final String SCENARIO     = "Scenario:";
   public static final String STEP         = "Step:";
   public static final String SETUPSTEP    = "SetupStep:";
   public static final String TEARDOWNSTEP = "TearDownStep:";
   public static final String STARTED      = "started";
   public static final String FAILED       = "failed";
   public static final String PASSED       = "passed";
   public static final String ERROR        = "threw error";
   public static final String COMPLETED    = "completed";

   private boolean            initialized  = false;

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

      initialized = true;
      return true;
   }

   public String getFeatureResultLog( FeatureResult result )
   {
      return result.isError() ? ERROR : ( result.isPassed() ? PASSED : FAILED );
   }

   public String getScenarioResultLog( ScenarioResult result )
   {
      return result.isError() ? ERROR : ( result.isPassed() ? PASSED : FAILED );
   }

   public String getStepResultLog( StepResult result )
   {
      return result.isError() ? ERROR : ( result.isPassed() ? PASSED : FAILED );
   }

   @Override
   public void processEvent( Event event )
   {
      switch ( event.getEventType() )
      {
         case StandardEventsTypes.RUN_START_EVENT:
            log.info( "{" + event.getRunName() + "}" + SPACE + STARTED );
            break;

         case StandardEventsTypes.RUN_COMPLETE_EVENT:
            log.info( "{" + event.getRunName() + "}" + SPACE + COMPLETED );
            break;

         case StandardEventsTypes.FEATURE_START_EVENT:
            FeatureStartEvent featureStartEvent = (FeatureStartEvent) event;
            log.info( "{" + featureStartEvent.getRunName() + "}{" + featureStartEvent.getFeature().getName() + "}" + SPACE + STARTED );
            break;

         case StandardEventsTypes.FEATURE_SETUP_STEP_START_EVENT:
            FeatureSetupStepStartEvent featureSetupStepStartEvent = (FeatureSetupStepStartEvent) event;
            log.info( "{" + featureSetupStepStartEvent.getRunName() + "}{" + featureSetupStepStartEvent.getFeature().getName() + "}{setup(" + featureSetupStepStartEvent.getStep().getIdentifier() + ")}" + SPACE + STARTED );
            break;

         case StandardEventsTypes.FEATURE_SETUP_STEP_COMPLETE_EVENT:
            FeatureSetupStepCompleteEvent featureSetupStepCompleteEvent = (FeatureSetupStepCompleteEvent) event;
            log.info( "{" + featureSetupStepCompleteEvent.getRunName() + "}{" + featureSetupStepCompleteEvent.getFeature().getName() + "}{setup(" + featureSetupStepCompleteEvent.getStep().getIdentifier() + ")}" + SPACE
                      + getStepResultLog( featureSetupStepCompleteEvent.getResult() ) );
            break;

         case StandardEventsTypes.SCENARIO_START_EVENT:
            ScenarioStartEvent scenarioStartEvent = (ScenarioStartEvent) event;
            log.info( "{" + scenarioStartEvent.getRunName() + "}{" + scenarioStartEvent.getFeatureName() + "}{" + scenarioStartEvent.getIterationNumber() + "}{" + scenarioStartEvent.getScenario().getName() + "}" + SPACE + STARTED );
            break;

         case StandardEventsTypes.SCENARIO_SETUP_STEP_START_EVENT:
            ScenarioSetupStepStartEvent scenarioSetupStepStartEvent = (ScenarioSetupStepStartEvent) event;
            log.info( "{" + scenarioSetupStepStartEvent.getRunName() + "}{" + scenarioSetupStepStartEvent.getFeatureName() + "}{" + scenarioSetupStepStartEvent.getScenarioName() + "}{" + scenarioSetupStepStartEvent.getIterationNumber() + "}{setup("
                      + scenarioSetupStepStartEvent.getStep().getIdentifier() + ")}" + SPACE + STARTED );
            break;
         case StandardEventsTypes.SCENARIO_SETUP_STEP_COMPLETE_EVENT:
            ScenarioSetupStepCompleteEvent scenarioSetupStepCompleteEvent = (ScenarioSetupStepCompleteEvent) event;
            log.info( "{" + scenarioSetupStepCompleteEvent.getRunName() + "}{" + scenarioSetupStepCompleteEvent.getFeatureName() + "}{" + scenarioSetupStepCompleteEvent.getScenarioName() + "}{" + scenarioSetupStepCompleteEvent.getIterationNumber()
                      + "}{setup(" + scenarioSetupStepCompleteEvent.getStep().getIdentifier() + ")}" + SPACE + getStepResultLog( scenarioSetupStepCompleteEvent.getResult() ) );
            break;

         case StandardEventsTypes.SCENARIO_CHAOS_ACTION_START_EVENT:
            ScenarioChaosActionStartEvent scenarioChaosActionStartEvent = (ScenarioChaosActionStartEvent) event;
            log.info( "{" + scenarioChaosActionStartEvent.getRunName() + "}{" + scenarioChaosActionStartEvent.getFeatureName() + "}{" + scenarioChaosActionStartEvent.getScenarioName() + "}{" + scenarioChaosActionStartEvent.getIterationNumber()
                      + "}{chaosAction(" + scenarioChaosActionStartEvent.getPreparedChaosAction().getName() + ")}" + SPACE + STARTED );
            break;
         case StandardEventsTypes.SCENARIO_CHAOS_ACTION_COMPLETE_EVENT:
            ScenarioChaosActionCompleteEvent scenarioChaosActionCompleteEvent = (ScenarioChaosActionCompleteEvent) event;
            log.info( "{" + scenarioChaosActionCompleteEvent.getRunName() + "}{" + scenarioChaosActionCompleteEvent.getFeatureName() + "}{" + scenarioChaosActionCompleteEvent.getScenarioName() + "}{" + scenarioChaosActionCompleteEvent.getIterationNumber()
                      + "}{chaosAction(" + scenarioChaosActionCompleteEvent.getPreparedChaosAction().getName() + ")}" + SPACE + getStepResultLog( scenarioChaosActionCompleteEvent.getResult() ) );
            break;

         case StandardEventsTypes.SCENARIO_STEP_START_EVENT:
            ScenarioStepStartEvent scenarioStepStartEvent = (ScenarioStepStartEvent) event;
            log.info( "{" + scenarioStepStartEvent.getRunName() + "}{" + scenarioStepStartEvent.getFeatureName() + "}{" + scenarioStepStartEvent.getScenarioName() + "}{" + scenarioStepStartEvent.getIterationNumber() + "}{step("
                      + scenarioStepStartEvent.getStep().getIdentifier() + ")}" + SPACE + STARTED );
            break;

         case StandardEventsTypes.SCENARIO_STEP_COMPLETE_EVENT:
            ScenarioStepCompleteEvent scenarioStepCompleteEvent = (ScenarioStepCompleteEvent) event;
            log.info( "{" + scenarioStepCompleteEvent.getRunName() + "}{" + scenarioStepCompleteEvent.getFeatureName() + "}{" + scenarioStepCompleteEvent.getScenarioName() + "}{" + scenarioStepCompleteEvent.getIterationNumber() + "}{step("
                      + scenarioStepCompleteEvent.getStep().getIdentifier() + ")}" + SPACE + getStepResultLog( scenarioStepCompleteEvent.getResult() ) );
            break;

         case StandardEventsTypes.SCENARIO_TEARDOWN_STEP_START_EVENT:
            ScenarioTearDownStepStartEvent scenarioTearDownStepStartEvent = (ScenarioTearDownStepStartEvent) event;
            log.info( "{" + scenarioTearDownStepStartEvent.getRunName() + "}{" + scenarioTearDownStepStartEvent.getFeatureName() + "}{" + scenarioTearDownStepStartEvent.getScenarioName() + "}{" + scenarioTearDownStepStartEvent.getIterationNumber()
                      + "}{tearDown(" + scenarioTearDownStepStartEvent.getStep().getIdentifier() + ")}" + SPACE + STARTED );
            break;

         case StandardEventsTypes.SCENARIO_TEARDOWN_STEP_COMPLETE_EVENT:
            ScenarioTearDownStepCompleteEvent scenarioTearDownStepCompleteEvent = (ScenarioTearDownStepCompleteEvent) event;
            log.info( "{" + scenarioTearDownStepCompleteEvent.getRunName() + "}{" + scenarioTearDownStepCompleteEvent.getFeatureName() + "}{" + scenarioTearDownStepCompleteEvent.getScenarioName() + "}{"
                      + scenarioTearDownStepCompleteEvent.getIterationNumber() + "}{tearDown(" + scenarioTearDownStepCompleteEvent.getStep().getIdentifier() + ")}" + SPACE + getStepResultLog( scenarioTearDownStepCompleteEvent.getResult() ) );
            break;

         case StandardEventsTypes.SCENARIO_COMPLETE_EVENT:
            ScenarioCompleteEvent scenarioCompleteEvent = (ScenarioCompleteEvent) event;
            log.info( "{" + scenarioCompleteEvent.getRunName() + "}{" + scenarioCompleteEvent.getFeatureName() + "}{" + scenarioCompleteEvent.getIterationNumber() + "}{" + scenarioCompleteEvent.getScenario().getName() + "}" + SPACE + COMPLETED );
            break;
         case StandardEventsTypes.FEATURE_TEARDOWN_STEP_START_EVENT:
            FeatureTearDownStepStartEvent featureTearDownStepStartEvent = (FeatureTearDownStepStartEvent) event;
            log.info( "{" + featureTearDownStepStartEvent.getRunName() + "}{" + featureTearDownStepStartEvent.getFeature().getName() + "}{tearDown(" + featureTearDownStepStartEvent.getStep().getIdentifier() + ")}" + SPACE + STARTED );
            break;

         case StandardEventsTypes.FEATURE_TEARDOWN_STEP_COMPLETE_EVENT:
            FeatureTearDownStepCompleteEvent featureTearDownStepCompleteEvent = (FeatureTearDownStepCompleteEvent) event;
            log.info( "{" + featureTearDownStepCompleteEvent.getRunName() + "}{" + featureTearDownStepCompleteEvent.getFeature().getName() + "}{tearDown(" + featureTearDownStepCompleteEvent.getStep().getIdentifier() + ")}" + SPACE
                      + getStepResultLog( featureTearDownStepCompleteEvent.getResult() ) );
            break;

         case StandardEventsTypes.FEATURE_COMPLETE_EVENT:
            FeatureCompleteEvent featureCompleteEvent = (FeatureCompleteEvent) event;
            log.info( "{" + featureCompleteEvent.getRunName() + "}{" + featureCompleteEvent.getFeature().getName() + "}" + SPACE + getFeatureResultLog( featureCompleteEvent.getResult() ) );
            break;

         case StandardEventsTypes.JAVA_FEATURE_START_EVENT:
            JavaFeatureStartEvent javaFeatureStartEvent = (JavaFeatureStartEvent) event;
            log.info( "{" + javaFeatureStartEvent.getRunName() + "}{" + javaFeatureStartEvent.getFeatureName() + "}" + SPACE + STARTED );
            break;

         case StandardEventsTypes.JAVA_FEATURE_SETUP_START_EVENT:
            JavaFeatureSetupStartEvent javaFeatureSetupStartEvent = (JavaFeatureSetupStartEvent) event;
            log.info( "{" + javaFeatureSetupStartEvent.getRunName() + "}{" + javaFeatureSetupStartEvent.getFeatureName() + "}{setup(" + javaFeatureSetupStartEvent.getStepIdentifier() + ")}" + SPACE + STARTED );
            break;

         case StandardEventsTypes.JAVA_FEATURE_SETUP_COMPLETE_EVENT:
            JavaFeatureSetupCompleteEvent javaFeatureSetupCompleteEvent = (JavaFeatureSetupCompleteEvent) event;
            log.info( "{" + javaFeatureSetupCompleteEvent.getRunName() + "}{" + javaFeatureSetupCompleteEvent.getFeatureName() + "}{setup(" + javaFeatureSetupCompleteEvent.getStepIdentifier() + ")}" + SPACE
                      + getStepResultLog( javaFeatureSetupCompleteEvent.getResult() ) );
            break;

         case StandardEventsTypes.JAVA_SCENARIO_SETUP_START_EVENT:
            JavaScenarioSetupStartEvent javaScenarioSetupStartEvent = (JavaScenarioSetupStartEvent) event;
            log.info( "{" + javaScenarioSetupStartEvent.getRunName() + "}{" + javaScenarioSetupStartEvent.getFeatureName() + "}{" + javaScenarioSetupStartEvent.getScenarioName() + "}{" + javaScenarioSetupStartEvent.getIterationNumber() + "}{setup("
                      + javaScenarioSetupStartEvent.getStepIdentifier() + ")}" + SPACE + STARTED );
            break;

         case StandardEventsTypes.JAVA_SCENARIO_SETUP_COMPLETE_EVENT:
            JavaScenarioSetupCompleteEvent javaScenarioSetupCompleteEvent = (JavaScenarioSetupCompleteEvent) event;
            log.info( "{" + javaScenarioSetupCompleteEvent.getRunName() + "}{" + javaScenarioSetupCompleteEvent.getFeatureName() + "}{" + javaScenarioSetupCompleteEvent.getScenarioName() + "}{" + javaScenarioSetupCompleteEvent.getIterationNumber()
                      + "}{setup(" + javaScenarioSetupCompleteEvent.getStepIdentifier() + ")}" + SPACE + getStepResultLog( javaScenarioSetupCompleteEvent.getResult() ) );
            break;

         case StandardEventsTypes.JAVA_SCENARIO_CHAOS_ACTION_START_EVENT:
            JavaScenarioChaosActionStartEvent javaScenarioChaosActionStartEvent = (JavaScenarioChaosActionStartEvent) event;
            log.info( "{" + javaScenarioChaosActionStartEvent.getRunName() + "}{" + javaScenarioChaosActionStartEvent.getFeatureName() + "}{" + javaScenarioChaosActionStartEvent.getScenarioName() + "}{"
                      + javaScenarioChaosActionStartEvent.getIterationNumber() + "}{chaosAction(" + javaScenarioChaosActionStartEvent.getChaosAction().getName() + ")}" + SPACE + STARTED );
            break;

         case StandardEventsTypes.JAVA_SCENARIO_CHAOS_ACTION_COMPLETE_EVENT:
            JavaScenarioChaosActionCompleteEvent javaScenarioChaosActionCompleteEvent = (JavaScenarioChaosActionCompleteEvent) event;
            log.info( "{" + javaScenarioChaosActionCompleteEvent.getRunName() + "}{" + javaScenarioChaosActionCompleteEvent.getFeatureName() + "}{" + javaScenarioChaosActionCompleteEvent.getScenarioName() + "}{"
                      + javaScenarioChaosActionCompleteEvent.getIterationNumber() + "}{chaosAction(" + javaScenarioChaosActionCompleteEvent.getChaosAction().getName() + ")}" + SPACE + getStepResultLog( javaScenarioChaosActionCompleteEvent.getResult() ) );
            break;

         case StandardEventsTypes.JAVA_SCENARIO_START_EVENT:
            JavaScenarioStartEvent javaScenarioStartEvent = (JavaScenarioStartEvent) event;
            log.info( "{" + javaScenarioStartEvent.getRunName() + "}{" + javaScenarioStartEvent.getFeatureName() + "}{" + javaScenarioStartEvent.getScenarioName() + "}{" + javaScenarioStartEvent.getIterationNumber() + "}" + SPACE + STARTED );
            break;

         case StandardEventsTypes.JAVA_SCENARIO_COMPLETE_EVENT:
            JavaScenarioCompleteEvent javaScenarioCompleteEvent = (JavaScenarioCompleteEvent) event;
            log.info( "{" + javaScenarioCompleteEvent.getRunName() + "}{" + javaScenarioCompleteEvent.getFeatureName() + "}{" + javaScenarioCompleteEvent.getScenarioName() + "}{" + javaScenarioCompleteEvent.getIterationNumber() + "}" + SPACE
                      + getScenarioResultLog( javaScenarioCompleteEvent.getResult() ) );
            break;

         case StandardEventsTypes.JAVA_SCENARIO_TEARDOWN_START_EVENT:
            JavaScenarioTearDownStartEvent javaScenarioTearDownStartEvent = (JavaScenarioTearDownStartEvent) event;
            log.info( "{" + javaScenarioTearDownStartEvent.getRunName() + "}{" + javaScenarioTearDownStartEvent.getFeatureName() + "}{" + javaScenarioTearDownStartEvent.getScenarioName() + "}{" + javaScenarioTearDownStartEvent.getIterationNumber()
                      + "}{tearDown(" + javaScenarioTearDownStartEvent.getStepIdentifier() + ")}" + SPACE + STARTED );
            break;

         case StandardEventsTypes.JAVA_SCENARIO_TEARDOWN_COMPLETE_EVENT:
            JavaScenarioTearDownCompleteEvent javaScenarioTearDownCompleteEvent = (JavaScenarioTearDownCompleteEvent) event;
            log.info( "{" + javaScenarioTearDownCompleteEvent.getRunName() + "}{" + javaScenarioTearDownCompleteEvent.getFeatureName() + "}{" + javaScenarioTearDownCompleteEvent.getScenarioName() + "}{"
                      + javaScenarioTearDownCompleteEvent.getIterationNumber() + "}{tearDown(" + javaScenarioTearDownCompleteEvent.getStepIdentifier() + ")}" + SPACE + getStepResultLog( javaScenarioTearDownCompleteEvent.getResult() ) );
            break;

         case StandardEventsTypes.JAVA_FEATURE_TEARDOWN_START_EVENT:
            JavaFeatureTearDownCompleteEvent javaFeatureTearDownCompleteEvent = (JavaFeatureTearDownCompleteEvent) event;
            log.info( "{" + javaFeatureTearDownCompleteEvent.getRunName() + "}{" + javaFeatureTearDownCompleteEvent.getFeatureName() + "}{tearDown(" + javaFeatureTearDownCompleteEvent.getStepIdentifier() + ")}" + SPACE
                      + getStepResultLog( javaFeatureTearDownCompleteEvent.getResult() ) );
            break;

         case StandardEventsTypes.JAVA_FEATURE_TEARDOWN_COMPLETE_EVENT:
            JavaFeatureTearDownStartEvent javaFeatureTearDownStartEvent = (JavaFeatureTearDownStartEvent) event;
            log.info( "{" + javaFeatureTearDownStartEvent.getRunName() + "}{" + javaFeatureTearDownStartEvent.getFeatureName() + "}{tearDown(" + javaFeatureTearDownStartEvent.getStepIdentifier() + ")}" + SPACE + STARTED );

            break;

         case StandardEventsTypes.JAVA_FEATURE_COMPLETE_EVENT:
            JavaFeatureCompleteEvent javaFeatureCompleteEvent = (JavaFeatureCompleteEvent) event;
            log.info( "{" + javaFeatureCompleteEvent.getRunName() + "}{" + javaFeatureCompleteEvent.getFeatureName() + "}" + SPACE + COMPLETED );
            break;

         case StandardEventsTypes.JOB_STEP_START_EVENT:
            JobStepStartEvent jobStepStartEvent = (JobStepStartEvent) event;
            log.info( "{" + jobStepStartEvent.getRunName() + "}{" + jobStepStartEvent.getFeatureName() + "}{" + jobStepStartEvent.getJob().getName() + "}{" + jobStepStartEvent.getIterationNumber() + "}{step(" + jobStepStartEvent.getStep().getIdentifier()
                      + ")}" + SPACE + STARTED );
            break;

         case StandardEventsTypes.JOB_STEP_COMPLETE_EVENT:
            JobStepCompleteEvent jobStepCompleteEvent = (JobStepCompleteEvent) event;
            log.info( "{" + jobStepCompleteEvent.getRunName() + "}{" + jobStepCompleteEvent.getFeatureName() + "}{" + jobStepCompleteEvent.getJob().getName() + "}{" + jobStepCompleteEvent.getIterationNumber() + "}{step("
                      + jobStepCompleteEvent.getStep().getIdentifier() + ")}" + SPACE + getStepResultLog( jobStepCompleteEvent.getResult() ) );
            break;

         case StandardEventsTypes.CHAOS_ACTION_JOB_COMPLETE_EVENT:
            ChaosActionJobCompleteEvent chaosActionJobCompleteEvent = (ChaosActionJobCompleteEvent) event;
            log.info( "{" + chaosActionJobCompleteEvent.getRunName() + "}{" + chaosActionJobCompleteEvent.getFeatureName() + "}{" + chaosActionJobCompleteEvent.getJob().getName() + "}{" + chaosActionJobCompleteEvent.getIterationNumber() + "}{step("
                      + chaosActionJobCompleteEvent.getChaosAction().getName() + ")}" + SPACE + getStepResultLog( chaosActionJobCompleteEvent.getResult() ) );
            break;

         case StandardEventsTypes.CHAOS_ACTION_JOB_START_EVENT:
            ChaosActionJobStartEvent chaosActionJobStartEvent = (ChaosActionJobStartEvent) event;
            log.info( "{" + chaosActionJobStartEvent.getRunName() + "}{" + chaosActionJobStartEvent.getFeatureName() + "}{" + chaosActionJobStartEvent.getJob().getName() + "}{" + chaosActionJobStartEvent.getIterationNumber() + "}{step("
                      + chaosActionJobStartEvent.getChaosAction().getName() + ")}" + SPACE + STARTED );
            break;

         case StandardEventsTypes.TEST_INCIDENT_OCCURRENCE_EVENT:
            TestIncidentOccurrenceEvent testIncidentOccurenceEvent = (TestIncidentOccurrenceEvent) event;
            log.info( "Incident occured: " + testIncidentOccurenceEvent.getIncident() );
            break;

         // case StandardEventsTypes.UNDEFINED:
         // case StandardEventsTypes.GENERIC_TEST_EVENT:
         default:
            log.info( "{" + event.getRunName() + "}{" + event.getEventType() + SPACE + event );
            break;
      }
   }
}
