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

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ( ( pluginName == null ) ? 0 : pluginName.hashCode() );
      return result;
   }

   @Override
   public boolean equals( Object obj )
   {
      if ( this == obj )
         return true;
      if ( obj == null )
         return false;
      if ( getClass() != obj.getClass() )
         return false;
      PluginConfig other = (PluginConfig) obj;
      if ( pluginName == null )
      {
         if ( other.pluginName != null )
            return false;
      }
      else if ( !pluginName.equals( other.pluginName ) )
         return false;
      return true;
   }

}
