package org.mvss.karta.framework.runtime.impl;

import java.io.Serializable;
import java.util.HashMap;

import org.mvss.karta.framework.runtime.Configurator;
import org.mvss.karta.framework.runtime.event.Event;
import org.mvss.karta.framework.runtime.event.FeatureCompleteEvent;
import org.mvss.karta.framework.runtime.event.FeatureSetupStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.FeatureSetupStepStartEvent;
import org.mvss.karta.framework.runtime.event.FeatureStartEvent;
import org.mvss.karta.framework.runtime.event.FeatureTearDownStepCompleteEvent;
import org.mvss.karta.framework.runtime.event.FeatureTearDownStepStartEvent;
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
import org.mvss.karta.framework.runtime.interfaces.TestEventListener;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DefaultTestEventListener implements TestEventListener
{
   public static final String PLUGIN_NAME  = "KartaDefaultTestEventListener";

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
   public boolean initialize( HashMap<String, HashMap<String, Serializable>> properties ) throws Throwable
   {
      if ( initialized )
      {
         return true;
      }

      Configurator.loadProperties( properties, this );

      log.debug( "Initializing Yerkin plugin with " + properties );

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
      else
      {
         log.info( "{" + event.getRunName() + "}{" + event.getEventType() + SPACE + event );
      }
   }
}
