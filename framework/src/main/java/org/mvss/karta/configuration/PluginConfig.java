package org.mvss.karta.configuration;

import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginConfig
{
   private String            pluginName;
   private ArrayList<String> pluginTypes;
   private String            className;
}
