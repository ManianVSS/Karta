package org.mvss.karta.samples.stepdefinitions;

import org.mvss.karta.framework.core.AfterFeature;
import org.mvss.karta.framework.core.AfterRun;
import org.mvss.karta.framework.core.AfterScenario;
import org.mvss.karta.framework.core.BeforeFeature;
import org.mvss.karta.framework.core.BeforeRun;
import org.mvss.karta.framework.core.BeforeScenario;
import org.mvss.karta.framework.core.PreparedScenario;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.samples.resources.AutomationDriver;
import org.mvss.karta.samples.resources.AutomationDriverImpl;

import lombok.extern.log4j.Log4j2;

/**
 * This class defines the life cycle hooks for Karta using Kriya plug-in. </br>
 * 
 * @author Manian
 */
@Log4j2
public class Hooks
{
   @BeforeRun( ".*" )
   public void beforeRun( String runName )
   {
      log.info( "@BeforeRun Kriya tag check " + runName );
   }

   @BeforeFeature( "cy.*" )
   public void beforeCycleFeatures( String runName, TestFeature feature )
   {
      log.info( "@BeforeFeature Kriya tag check " + runName + " " + feature.getName() );
   }

   @BeforeScenario( "UI" )
   public void beforeUIScenarios( String runName, String featureName, PreparedScenario scenario )
   {
      log.info( "@BeforeScenario Kriya tag check " + runName + " " + featureName + " " + scenario.getName() );
      AutomationDriver automationDriver = new AutomationDriverImpl( runName, featureName, scenario.getName() );
      scenario.getContextBeanRegistry().add( "AutomationDriverObject", automationDriver );
   }

   @AfterScenario( "UI" )
   public void afterUIScenarios( String runName, String featureName, PreparedScenario scenario )
   {
      log.info( "@AfterScenario Kriya tag check " + runName + " " + featureName + " " + scenario.getName() );
      try
      {
         ( (AutomationDriver) scenario.getContextBeanRegistry().get( "AutomationDriverObject" ) ).close();
      }
      catch ( Exception e )
      {
         log.error( "", e );
      }
   }

   @AfterFeature( "cy.*" )
   public void afterCycleFeatures( String runName, TestFeature feature )
   {
      log.info( "@AfterFeature Kriya tag check " + runName + " " + feature.getName() );
   }

   @AfterRun( ".*" )
   public void afterRun( String runName )
   {
      log.info( "@AfterRun Kriya tag check " + runName );
   }

}
