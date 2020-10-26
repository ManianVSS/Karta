package org.mvss.karta.framework.runtime;

public class Constants
{
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

   public static final String   GENERIC_FEATURE                  = "__generic_feature__";
   public static final String   FEATURE_SETUP                    = "__feature_setup__";
   public static final String   GENERIC_SCENARIO                 = "__generic_scenario__";
   public static final String   GENERIC_STEP                     = "__generic_step__";
   public static final String   FEATURE_TEARDOWN                 = "__feature_teardown__";
   public static final String   JOB                              = "job:";
   public static final String   _SETUP_                          = ":Setup:";
   public static final String   _TEARDOWN_                       = ":TearDown:";
}
