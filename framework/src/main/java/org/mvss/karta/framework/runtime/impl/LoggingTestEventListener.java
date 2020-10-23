package org.mvss.karta.framework.runtime.impl;

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
import org.mvss.karta.framework.runtime.event.RunCompleteEvent;
import org.mvss.karta.framework.runtime.event.RunStartEvent;
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
import org.mvss.karta.framework.runtime.event.TestIncidentOccurenceEvent;
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

   @Override
   public void processEvent( Event event )
   {
      if ( event instanceof RunStartEvent )
      {
         log.info( "{" + event.getRunName() + "}" + SPACE + STARTED );
      }
      else if ( event instanceof RunCompleteEvent )
      {
         log.info( "{" + event.getRunName() + "}" + SPACE + COMPLETED );
      }
      else if ( event instanceof FeatureStartEvent )
      {
         FeatureStartEvent featureStartEvent = (FeatureStartEvent) event;
         log.info( "{" + featureStartEvent.getRunName() + "}{" + featureStartEvent.getFeature().getName() + "}" + SPACE + STARTED );
      }
      else if ( event instanceof FeatureCompleteEvent )
      {
         FeatureCompleteEvent featureStartEvent = (FeatureCompleteEvent) event;
         log.info( "{" + featureStartEvent.getRunName() + "}{" + featureStartEvent.getFeature().getName() + "}" + SPACE + COMPLETED );
      }
      else if ( event instanceof FeatureSetupStepStartEvent )
      {
         FeatureSetupStepStartEvent stepStartEvent = (FeatureSetupStepStartEvent) event;
         log.info( "{" + stepStartEvent.getRunName() + "}{" + stepStartEvent.getFeature().getName() + "}{setup(" + stepStartEvent.getSetupStep().getIdentifier() + ")}" + SPACE + STARTED );
      }
      else if ( event instanceof FeatureSetupStepCompleteEvent )
      {
         FeatureSetupStepCompleteEvent stepCompleteEvent = (FeatureSetupStepCompleteEvent) event;
         log.info( "{" + stepCompleteEvent.getRunName() + "}{" + stepCompleteEvent.getFeature().getName() + "}{setup(" + stepCompleteEvent.getSetupStep().getIdentifier() + ")}" + SPACE
                   + ( stepCompleteEvent.getResult().isSuccesssful() ? PASSED : FAILED ) );
      }
      else if ( event instanceof ScenarioStartEvent )
      {
         ScenarioStartEvent scenarioStartEvent = (ScenarioStartEvent) event;
         log.info( "{" + scenarioStartEvent.getRunName() + "}{" + scenarioStartEvent.getFeature().getName() + "}{" + scenarioStartEvent.getIterationNumber() + "}{" + scenarioStartEvent.getScenario().getName() + "}" + SPACE + STARTED );
      }
      else if ( event instanceof ScenarioSetupStepStartEvent )
      {
         ScenarioSetupStepStartEvent stepStartEvent = (ScenarioSetupStepStartEvent) event;
         log.info( "{" + stepStartEvent.getRunName() + "}{" + stepStartEvent.getFeature().getName() + "}{" + stepStartEvent.getScenario().getName() + "}{" + stepStartEvent.getIterationNumber() + "}{setup("
                   + stepStartEvent.getScenarioSetupStep().getIdentifier() + ")}" + SPACE + STARTED );
      }
      else if ( event instanceof ScenarioSetupStepCompleteEvent )
      {
         ScenarioSetupStepCompleteEvent stepCompleteEvent = (ScenarioSetupStepCompleteEvent) event;
         log.info( "{" + stepCompleteEvent.getRunName() + "}{" + stepCompleteEvent.getFeature().getName() + "}{" + stepCompleteEvent.getScenario().getName() + "}{" + stepCompleteEvent.getIterationNumber() + "}{setup("
                   + stepCompleteEvent.getScenarioSetupStep().getIdentifier() + ")}" + SPACE + ( stepCompleteEvent.getResult().isSuccesssful() ? PASSED : FAILED ) );
      }
      else if ( event instanceof ScenarioChaosActionStartEvent )
      {
         ScenarioChaosActionStartEvent chaosActionStartEvent = (ScenarioChaosActionStartEvent) event;
         log.info( "{" + chaosActionStartEvent.getRunName() + "}{" + chaosActionStartEvent.getFeature().getName() + "}{" + chaosActionStartEvent.getScenario().getName() + "}{" + chaosActionStartEvent.getIterationNumber() + "}{chaosAction("
                   + chaosActionStartEvent.getChaosAction().getName() + ")}" + SPACE + STARTED );
      }
      else if ( event instanceof ScenarioChaosActionCompleteEvent )
      {
         ScenarioChaosActionCompleteEvent chaosActionCopmleteEvent = (ScenarioChaosActionCompleteEvent) event;
         log.info( "{" + chaosActionCopmleteEvent.getRunName() + "}{" + chaosActionCopmleteEvent.getFeature().getName() + "}{" + chaosActionCopmleteEvent.getScenario().getName() + "}{" + chaosActionCopmleteEvent.getIterationNumber() + "}{chaosAction("
                   + chaosActionCopmleteEvent.getChaosAction().getName() + ")}" + SPACE + ( chaosActionCopmleteEvent.getResult().isSuccesssful() ? PASSED : FAILED ) );
      }
      else if ( event instanceof ScenarioStepStartEvent )
      {
         ScenarioStepStartEvent stepStartEvent = (ScenarioStepStartEvent) event;
         log.info( "{" + stepStartEvent.getRunName() + "}{" + stepStartEvent.getFeature().getName() + "}{" + stepStartEvent.getScenario().getName() + "}{" + stepStartEvent.getIterationNumber() + "}{step(" + stepStartEvent.getScenarioStep().getIdentifier()
                   + ")}" + SPACE + STARTED );
      }
      else if ( event instanceof ScenarioStepCompleteEvent )
      {
         ScenarioStepCompleteEvent stepCompleteEvent = (ScenarioStepCompleteEvent) event;
         log.info( "{" + stepCompleteEvent.getRunName() + "}{" + stepCompleteEvent.getFeature().getName() + "}{" + stepCompleteEvent.getScenario().getName() + "}{" + stepCompleteEvent.getIterationNumber() + "}{step("
                   + stepCompleteEvent.getScenarioStep().getIdentifier() + ")}" + SPACE + ( stepCompleteEvent.getResult().isSuccesssful() ? PASSED : FAILED ) );
      }
      else if ( event instanceof ScenarioTearDownStepStartEvent )
      {
         ScenarioTearDownStepStartEvent stepStartEvent = (ScenarioTearDownStepStartEvent) event;
         log.info( "{" + stepStartEvent.getRunName() + "}{" + stepStartEvent.getFeature().getName() + "}{" + stepStartEvent.getScenario().getName() + "}{" + stepStartEvent.getIterationNumber() + "}{tearDown("
                   + stepStartEvent.getScenarioTearDownStep().getIdentifier() + ")}" + SPACE + STARTED );
      }
      else if ( event instanceof ScenarioTearDownStepCompleteEvent )
      {
         ScenarioTearDownStepCompleteEvent stepCompleteEvent = (ScenarioTearDownStepCompleteEvent) event;
         log.info( "{" + stepCompleteEvent.getRunName() + "}{" + stepCompleteEvent.getFeature().getName() + "}{" + stepCompleteEvent.getScenario().getName() + "}{" + stepCompleteEvent.getIterationNumber() + "}{tearDown("
                   + stepCompleteEvent.getScenarioTearDownStep().getIdentifier() + ")}" + SPACE + ( stepCompleteEvent.getResult().isSuccesssful() ? PASSED : FAILED ) );
      }
      else if ( event instanceof ScenarioCompleteEvent )
      {
         ScenarioCompleteEvent scenarioCompleteEvent = (ScenarioCompleteEvent) event;
         log.info( "{" + scenarioCompleteEvent.getRunName() + "}{" + scenarioCompleteEvent.getFeature().getName() + "}{" + scenarioCompleteEvent.getIterationNumber() + "}{" + scenarioCompleteEvent.getScenario().getName() + "}" + SPACE + COMPLETED );
      }
      else if ( event instanceof FeatureTearDownStepStartEvent )
      {
         FeatureTearDownStepStartEvent featureTearDownStepStartEvent = (FeatureTearDownStepStartEvent) event;
         log.info( "{" + featureTearDownStepStartEvent.getRunName() + "}{" + featureTearDownStepStartEvent.getFeature().getName() + "}{tearDown(" + featureTearDownStepStartEvent.getTearDownStep().getIdentifier() + ")}" + SPACE + STARTED );
      }
      else if ( event instanceof FeatureTearDownStepCompleteEvent )
      {
         FeatureTearDownStepCompleteEvent featureTearDownStepCompleteEvent = (FeatureTearDownStepCompleteEvent) event;
         log.info( "{" + featureTearDownStepCompleteEvent.getRunName() + "}{" + featureTearDownStepCompleteEvent.getFeature().getName() + "}{tearDown(" + featureTearDownStepCompleteEvent.getTearDownStep().getIdentifier() + ")}" + SPACE
                   + ( featureTearDownStepCompleteEvent.getResult().isSuccesssful() ? PASSED : FAILED ) );
      }
      else if ( event instanceof JavaFeatureStartEvent )
      {
         JavaFeatureStartEvent featureStartEvent = (JavaFeatureStartEvent) event;
         log.info( "{" + featureStartEvent.getRunName() + "}{" + featureStartEvent.getFeatureName() + "}" + SPACE + STARTED );
      }
      else if ( event instanceof JavaFeatureCompleteEvent )
      {
         JavaFeatureCompleteEvent featureStartEvent = (JavaFeatureCompleteEvent) event;
         log.info( "{" + featureStartEvent.getRunName() + "}{" + featureStartEvent.getFeatureName() + "}" + SPACE + COMPLETED );
      }
      else if ( event instanceof JavaFeatureSetupStartEvent )
      {
         JavaFeatureSetupStartEvent stepStartEvent = (JavaFeatureSetupStartEvent) event;
         log.info( "{" + stepStartEvent.getRunName() + "}{" + stepStartEvent.getFeatureName() + "}{setup(" + stepStartEvent.getMethodName() + ")}" + SPACE + STARTED );
      }
      else if ( event instanceof JavaFeatureSetupCompleteEvent )
      {
         JavaFeatureSetupCompleteEvent stepCompleteEvent = (JavaFeatureSetupCompleteEvent) event;
         log.info( "{" + stepCompleteEvent.getRunName() + "}{" + stepCompleteEvent.getFeatureName() + "}{setup(" + stepCompleteEvent.getMethodName() + ")}" + SPACE + ( stepCompleteEvent.getResult().isSuccesssful() ? PASSED : FAILED ) );
      }
      else if ( event instanceof JavaScenarioSetupStartEvent )
      {
         JavaScenarioSetupStartEvent stepStartEvent = (JavaScenarioSetupStartEvent) event;
         log.info( "{" + stepStartEvent.getRunName() + "}{" + stepStartEvent.getFeatureName() + "}{" + stepStartEvent.getScenarioName() + "}{" + stepStartEvent.getIterationNumber() + "}{setup(" + stepStartEvent.getMethod() + ")}" + SPACE + STARTED );
      }
      else if ( event instanceof JavaScenarioSetupCompleteEvent )
      {
         JavaScenarioSetupCompleteEvent stepCompleteEvent = (JavaScenarioSetupCompleteEvent) event;
         log.info( "{" + stepCompleteEvent.getRunName() + "}{" + stepCompleteEvent.getFeatureName() + "}{" + stepCompleteEvent.getScenarioName() + "}{" + stepCompleteEvent.getIterationNumber() + "}{setup(" + stepCompleteEvent.getMethod() + ")}" + SPACE
                   + ( stepCompleteEvent.getResult().isSuccesssful() ? PASSED : FAILED ) );
      }
      else if ( event instanceof JavaScenarioChaosActionStartEvent )
      {
         JavaScenarioChaosActionStartEvent chaosActionStartEvent = (JavaScenarioChaosActionStartEvent) event;
         log.info( "{" + chaosActionStartEvent.getRunName() + "}{" + chaosActionStartEvent.getFeatureName() + "}{" + chaosActionStartEvent.getScenarioName() + "}{" + chaosActionStartEvent.getIterationNumber() + "}{chaosAction("
                   + chaosActionStartEvent.getChaosAction().getName() + ")}" + SPACE + STARTED );
      }
      else if ( event instanceof JavaScenarioChaosActionCompleteEvent )
      {
         JavaScenarioChaosActionCompleteEvent chaosActionCopmleteEvent = (JavaScenarioChaosActionCompleteEvent) event;
         log.info( "{" + chaosActionCopmleteEvent.getRunName() + "}{" + chaosActionCopmleteEvent.getFeatureName() + "}{" + chaosActionCopmleteEvent.getScenarioName() + "}{" + chaosActionCopmleteEvent.getIterationNumber() + "}{chaosAction("
                   + chaosActionCopmleteEvent.getChaosAction().getName() + ")}" + SPACE + ( chaosActionCopmleteEvent.getResult().isSuccesssful() ? PASSED : FAILED ) );
      }
      else if ( event instanceof JavaScenarioStartEvent )
      {
         JavaScenarioStartEvent stepStartEvent = (JavaScenarioStartEvent) event;
         log.info( "{" + stepStartEvent.getRunName() + "}{" + stepStartEvent.getFeatureName() + "}{" + stepStartEvent.getScenarioName() + "}{" + stepStartEvent.getIterationNumber() + "}{step(" + stepStartEvent.getMethod() + ")}" + SPACE + STARTED );
      }
      else if ( event instanceof JavaScenarioCompleteEvent )
      {
         JavaScenarioCompleteEvent stepCompleteEvent = (JavaScenarioCompleteEvent) event;
         log.info( "{" + stepCompleteEvent.getRunName() + "}{" + stepCompleteEvent.getFeatureName() + "}{" + stepCompleteEvent.getScenarioName() + "}{" + stepCompleteEvent.getIterationNumber() + "}{step(" + stepCompleteEvent.getMethod() + ")}" + SPACE
                   + ( stepCompleteEvent.getResult().isSuccesssful() ? PASSED : FAILED ) );
      }
      else if ( event instanceof JavaScenarioTearDownStartEvent )
      {
         JavaScenarioTearDownStartEvent stepStartEvent = (JavaScenarioTearDownStartEvent) event;
         log.info( "{" + stepStartEvent.getRunName() + "}{" + stepStartEvent.getFeatureName() + "}{" + stepStartEvent.getScenarioName() + "}{" + stepStartEvent.getIterationNumber() + "}{tearDown(" + stepStartEvent.getMethod() + ")}" + SPACE + STARTED );
      }
      else if ( event instanceof JavaScenarioTearDownCompleteEvent )
      {
         JavaScenarioTearDownCompleteEvent stepCompleteEvent = (JavaScenarioTearDownCompleteEvent) event;
         log.info( "{" + stepCompleteEvent.getRunName() + "}{" + stepCompleteEvent.getFeatureName() + "}{" + stepCompleteEvent.getScenarioName() + "}{" + stepCompleteEvent.getIterationNumber() + "}{tearDown(" + stepCompleteEvent.getMethod() + ")}" + SPACE
                   + ( stepCompleteEvent.getResult().isSuccesssful() ? PASSED : FAILED ) );
      }
      else if ( event instanceof JavaFeatureTearDownStartEvent )
      {
         JavaFeatureTearDownStartEvent featureTearDownStepStartEvent = (JavaFeatureTearDownStartEvent) event;
         log.info( "{" + featureTearDownStepStartEvent.getRunName() + "}{" + featureTearDownStepStartEvent.getFeatureName() + "}{tearDown(" + featureTearDownStepStartEvent.getMethod() + ")}" + SPACE + STARTED );
      }
      else if ( event instanceof JavaFeatureTearDownCompleteEvent )
      {
         JavaFeatureTearDownCompleteEvent featureTearDownStepCompleteEvent = (JavaFeatureTearDownCompleteEvent) event;
         log.info( "{" + featureTearDownStepCompleteEvent.getRunName() + "}{" + featureTearDownStepCompleteEvent.getFeatureName() + "}{tearDown(" + featureTearDownStepCompleteEvent.getMethod() + ")}" + SPACE
                   + ( featureTearDownStepCompleteEvent.getResult().isSuccesssful() ? PASSED : FAILED ) );
      }
      else if ( event instanceof TestIncidentOccurenceEvent )
      {
         TestIncidentOccurenceEvent testIncidentOccurenceEvent = (TestIncidentOccurenceEvent) event;
         log.info( "Incident occured: " + testIncidentOccurenceEvent.getIncident() );
      }
      else
      {
         log.info( "{" + event.getRunName() + "}{" + event.getEventType() + SPACE + event );
      }
   }
}
