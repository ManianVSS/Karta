package org.mvss.karta.configuration;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginConfig implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            pluginName;
   private String            className;
   private String            jarFile;
}
