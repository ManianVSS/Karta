package org.mvss.karta.configuration;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Defines configuration of a plug-in.</br>
 * This configuration schema should be saved as /KartaPluginsConfiguration.yaml in the plug-in jar.</br>
 * 
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PluginConfig implements Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    * The name of the plug-in.
    */
   @Builder.Default
   private String            pluginName       = null;

   /**
    * The fully qualified java class name of the plug-in.
    */
   @Builder.Default
   private String            className        = null;

   /**
    * The jar file name for the plug-in.</br>
    * This attribute is relevant if defining plug-in configuration outside a jar file.
    */
   @Builder.Default
   private String            jarFile          = null;
}
