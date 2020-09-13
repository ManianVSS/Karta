package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuntimeConfiguration
{
   private String                                         featureSourceParserPlugin;

   private String                                         stepRunnerPlugin;

   private ArrayList<String>                              testDataSourcePlugins;    // = new ArrayList<String>( Arrays.asList( "Yerkin" ) );

   private HashMap<String, HashMap<String, Serializable>> pluginConfiguration;      // = new HashMap<String, HashMap<String, Serializable>>();

}
