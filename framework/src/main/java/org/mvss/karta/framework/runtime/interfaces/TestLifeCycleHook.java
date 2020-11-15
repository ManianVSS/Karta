package org.mvss.karta.framework.runtime.interfaces;

import java.util.HashSet;

import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestScenario;

public interface TestLifeCycleHook extends Plugin
{
   void runStart( String runName );

   void featureStart( String runName, TestFeature feature, HashSet<String> tags );

   void scenarioStart( String runName, String featureName, TestScenario scenario, HashSet<String> tags );

   void scenarioStop( String runName, String featureName, TestScenario scenario, HashSet<String> tags );

   void featureStop( String runName, TestFeature feature, HashSet<String> tags );

   void runStop( String runName );
}
