package org.mvss.karta.samples.stepdefinitions;

import org.mvss.karta.framework.core.AfterFeature;
import org.mvss.karta.framework.core.AfterScenario;
import org.mvss.karta.framework.core.BeforeFeature;
import org.mvss.karta.framework.core.BeforeScenario;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestScenario;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Hooks
{
   @BeforeFeature( "cycle" )
   public void beforeCycleFeatures( String runName, TestFeature feature )
   {
      log.info( "@BeforeFeature Kriya tag check " + runName + " " + feature.getName() );
   }

   @BeforeScenario( "UI" )
   public void beforeUIScenarios( String runName, String featureName, TestScenario scenario )
   {
      log.info( "@BeforeScenario Kriya tag check " + runName + " " + featureName + " " + scenario.getName() );
   }

   @AfterScenario( "UI" )
   public void afterUIScenarios( String runName, String featureName, TestScenario scenario )
   {
      log.info( "@AfterScenario Kriya tag check " + runName + " " + featureName + " " + scenario.getName() );
   }

   @AfterFeature( "cycle" )
   public void afterCycleFeatures( String runName, TestFeature feature )
   {
      log.info( "@AfterFeature Kriya tag check " + runName + " " + feature.getName() );
   }
}
