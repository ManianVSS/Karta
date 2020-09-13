package org.mvss.karta.configuration;

import java.io.Serializable;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginClassConfig
{
   private String                className;
   private String                jarFile;
   HashMap<String, Serializable> properties;
}
