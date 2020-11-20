package org.mvss.karta.framework.runtime.event;

public class StandardEventsTypes
{
   public static final String UNDEFINED                                 = "Undefined";
   public static final String GENERIC_TEST_EVENT                        = "GenericTestEvent";

   public static final String RUN_START_EVENT                           = "RunStartEvent";
   public static final String RUN_COMPLETE_EVENT                        = "RunCompleteEvent";

   public static final String FEATURE_START_EVENT                       = "FeatureStartEvent";
   public static final String FEATURE_SETUP_STEP_START_EVENT            = "FeatureSetupStepStartEvent";
   public static final String FEATURE_SETUP_STEP_COMPLETE_EVENT         = "FeatureSetupStepCompleteEvent";
   public static final String SCENARIO_START_EVENT                      = "ScenarioStartEvent";
   public static final String SCENARIO_SETUP_STEP_START_EVENT           = "ScenarioSetupStepStartEvent";
   public static final String SCENARIO_SETUP_STEP_COMPLETE_EVENT        = "ScenarioSetupStepCompleteEvent";
   public static final String SCENARIO_CHAOS_ACTION_START_EVENT         = "ScenarioChaosActionStartEvent";
   public static final String SCENARIO_CHAOS_ACTION_COMPLETE_EVENT      = "ScenarioChaosActionCompleteEvent";
   public static final String SCENARIO_STEP_START_EVENT                 = "ScenarioStepStartEvent";
   public static final String SCENARIO_STEP_COMPLETE_EVENT              = "ScenarioStepCompleteEvent";
   public static final String SCENARIO_TEARDOWN_STEP_START_EVENT        = "ScenarioTearDownStepStartEvent";
   public static final String SCENARIO_TEARDOWN_STEP_COMPLETE_EVENT     = "ScenarioTearDownStepCompleteEvent";
   public static final String SCENARIO_COMPLETE_EVENT                   = "ScenarioCompleteEvent";
   public static final String FEATURE_TEARDOWN_STEP_START_EVENT         = "FeatureTearDownStepStartEvent";
   public static final String FEATURE_TEARDOWN_STEP_COMPLETE_EVENT      = "FeatureTearDownStepCompleteEvent";
   public static final String FEATURE_COMPLETE_EVENT                    = "FeatureCompleteEvent";

   public static final String JAVA_FEATURE_START_EVENT                  = "JavaFeatureStartEvent";
   public static final String JAVA_FEATURE_SETUP_START_EVENT            = "JavaFeatureSetupStartEvent";
   public static final String JAVA_FEATURE_SETUP_COMPLETE_EVENT         = "JavaFeatureSetupCompleteEvent";
   public static final String JAVA_SCENARIO_SETUP_START_EVENT           = "JavaScenarioSetupStartEvent";
   public static final String JAVA_SCENARIO_SETUP_COMPLETE_EVENT        = "JavaScenarioSetupCompleteEvent";
   public static final String JAVA_SCENARIO_CHAOS_ACTION_START_EVENT    = "JavaScenarioChaosActionStartEvent";
   public static final String JAVA_SCENARIO_CHAOS_ACTION_COMPLETE_EVENT = "JavaScenarioChaosActionCompleteEvent";
   public static final String JAVA_SCENARIO_START_EVENT                 = "JavaScenarioStartEvent";
   public static final String JAVA_SCENARIO_COMPLETE_EVENT              = "JavaScenarioCompleteEvent";
   public static final String JAVA_SCENARIO_TEARDOWN_START_EVENT        = "JavaScenarioTearDownStartEvent";
   public static final String JAVA_SCENARIO_TEARDOWN_COMPLETE_EVENT     = "JavaScenarioTearDownCompleteEvent";
   public static final String JAVA_FEATURE_TEARDOWN_START_EVENT         = "JavaFeatureTearDownStartEvent";
   public static final String JAVA_FEATURE_TEARDOWN_COMPLETE_EVENT      = "JavaFeatureTearDownCompleteEvent";
   public static final String JAVA_FEATURE_COMPLETE_EVENT               = "JavaFeatureCompleteEvent";

   public static final String JOB_STEP_START_EVENT                      = "JobStepStartEvent";
   public static final String JOB_STEP_COMPLETE_EVENT                   = "JobStepCompleteEvent";
   public static final String CHAOS_ACTION_JOB_COMPLETE_EVENT           = "ChaosActionJobCompleteEvent";
   public static final String CHAOS_ACTION_JOB_START_EVENT              = "ChaosActionJobStartEvent";

   public static final String TEST_INCIDENT_OCCURRENCE_EVENT            = "TestIncidentOccurrenceEvent";
}
