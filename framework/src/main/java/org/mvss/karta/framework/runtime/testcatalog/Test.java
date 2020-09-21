package org.mvss.karta.framework.runtime.testcatalog;

import java.io.Serializable;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Test implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID      = 1L;

   private TestType          testType;

   private String            name;
   private String            description;

   @Builder.Default
   private HashSet<String>   tags                  = new HashSet<String>();

   private String            sourceArchive;

   private String            featureSourceParserPlugin;
   private String            stepRunnerPlugin;

   @Builder.Default
   private HashSet<String>   testDataSourcePlugins = new HashSet<String>();

   private String            featureFileName;

   private String            javaTestClass;

   public void propogateSourceArchive( String sa, String fspp, String srp, HashSet<String> tdsp )
   {
      if ( StringUtils.isEmpty( featureSourceParserPlugin ) && StringUtils.isNotEmpty( fspp ) )
      {
         featureSourceParserPlugin = fspp;
      }

      if ( StringUtils.isEmpty( stepRunnerPlugin ) && StringUtils.isNotEmpty( srp ) )
      {
         stepRunnerPlugin = srp;
      }

      if ( tdsp != null )
      {
         for ( String testDataSourcePlugin : tdsp )
         {
            if ( !testDataSourcePlugins.contains( testDataSourcePlugin ) )
            {
               testDataSourcePlugins.add( testDataSourcePlugin );
            }
         }
      }

      if ( StringUtils.isEmpty( sourceArchive ) && StringUtils.isNotEmpty( sa ) )
      {
         sourceArchive = sa;
      }
   }
}
