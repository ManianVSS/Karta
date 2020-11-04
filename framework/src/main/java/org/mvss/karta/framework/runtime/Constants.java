package org.mvss.karta.framework.runtime;

public class Constants
{
   public static final String   KARTA                            = "Karta";
   public static final String   KARTA_BASE_CONFIG_JSON           = "KartaBaseConfiguration.json";
   public static final String   KARTA_PLUGINS_CONFIG_JSON        = "KartaPluginsConfiguration.json";
   public static final String   KARTA_RUNTIME_CONFIGURATION_JSON = "KartaRuntimeConfiguration.json";
   public static final String   KARTA_RUNTIME_PROPERTIES_JSON    = "KartaRuntimeProperties.json";

   public static final String   KARTA_BASE_CONFIG_YAML           = "KartaBaseConfiguration.yaml";
   public static final String   KARTA_PLUGINS_CONFIG_YAML        = "KartaPluginsConfiguration.yaml";
   public static final String   KARTA_RUNTIME_CONFIGURATION_YAML = "KartaRuntimeConfiguration.yaml";
   public static final String   KARTA_RUNTIME_PROPERTIES_YAML    = "KartaRuntimeProperties.yaml";

   public static final String   TEST_CATALOG_FILE_NAME           = "KartaTestCatalog.yaml";
   public static final String   TEST_CATALOG_FRAGMENT_FILE_NAME  = "KartaTestCatalogFragment.yaml";

   public static final String   NULL_STRING                      = (String) null;
   public static final String   EMPTY_STRING                     = "";

   public static final String   DOT                              = ".";

   public static final String   JAR                              = "jar";

   public static final String   JSON                             = "json";
   public static final String   YAML                             = "yaml";
   public static final String   YML                              = "yml";
   public static final String   XML                              = "xml";
   public static final String   PROPERTIES                       = "properties";

   public static final String[] jarExtention                     = {JAR};
   public static final String[] dataFileExtentions               = {JSON, YAML, YML, XML};

   public static final String   UNNAMED                          = "Unnamed";

   public static final String   __GENERIC_FEATURE__              = "__generic_feature__";
   public static final String   __FEATURE_SETUP__                = "__feature_setup__";
   public static final String   __GENERIC_SCENARIO__             = "__generic_scenario__";
   public static final String   __GENERIC_STEP__                 = "__generic_step__";
   public static final String   __FEATURE_TEARDOWN__             = "__feature_teardown__";

   public static final String   JOB                              = "job:";
   public static final String   _SETUP_                          = ":Setup:";
   public static final String   _TEARDOWN_                       = ":TearDown:";

   public static final String   __TESTS__                        = "__tests__";

   public static final String   CONTENT_TYPE                     = "Content-Type";
   public static final String   APPLICATION_JSON                 = "application/json";
   public static final String   ACCEPT                           = "Accept";

   public static final String   PATH_HEALTH                      = "/health";
   public static final String   PATH_RUN_STEP                    = "/run/step";
   public static final String   PATH_RUN_CHAOS_ACTION            = "/run/choasAction";
   public static final String   PATH_RUN_SCENARIO                = "/run/scenario";
   public static final String   PATH_RUN_JOB                     = "/run/job";
   public static final String   PATH_RUN_FEATURE                 = "/run/feature";
}
