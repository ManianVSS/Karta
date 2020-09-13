package org.mvss.karta.configuration;

import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KartaConfiguration
{
   private HashMap<String, PluginClassConfig> featureSourceParserConfig;
   private HashMap<String, PluginClassConfig> stepRunnerConfig;
   private HashMap<String, PluginClassConfig> testDataSourceConfig;
}
