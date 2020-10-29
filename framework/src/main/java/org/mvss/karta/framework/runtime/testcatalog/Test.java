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
public class Test implements Serializable, Comparable<Test>
{

   /**
    * 
    */
   private static final long serialVersionUID              = 1L;

   @Builder.Default
   private TestType          testType                      = TestType.FEATURE;

   private String            name;
   private String            description;

   @Builder.Default
   private Integer           priority                      = Integer.MAX_VALUE;

   @Builder.Default
   private HashSet<String>   tags                          = new HashSet<String>();

   private String            sourceArchive;

   private String            featureSourceParserPlugin;
   private String            stepRunnerPlugin;

   @Builder.Default
   private HashSet<String>   testDataSourcePlugins         = new HashSet<String>();

   private String            featureFileName;

   private String            javaTestClass;

   @Builder.Default
   private Boolean           chanceBasedScenarioExecution  = false;

   @Builder.Default
   private Boolean           exclusiveScenarioPerIteration = false;

   @Builder.Default
   private long              numberOfIterations            = 1;

   @Builder.Default
   private int               numberOfThreads               = 1;

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

   public void mergeWithTest( Test test )
   {
      if ( test == null )
      {
         return;
      }

      if ( tags.isEmpty() && !test.tags.isEmpty() )
      {
         tags.addAll( test.tags );
      }

      if ( StringUtils.isEmpty( sourceArchive ) && StringUtils.isNotEmpty( test.sourceArchive ) )
      {
         sourceArchive = test.sourceArchive;
      }

      if ( StringUtils.isEmpty( featureSourceParserPlugin ) && StringUtils.isNotEmpty( test.featureSourceParserPlugin ) )
      {
         featureSourceParserPlugin = test.featureSourceParserPlugin;
      }

      if ( StringUtils.isEmpty( stepRunnerPlugin ) && StringUtils.isNotEmpty( test.stepRunnerPlugin ) )
      {
         stepRunnerPlugin = test.stepRunnerPlugin;
      }

      if ( testDataSourcePlugins.isEmpty() && !test.testDataSourcePlugins.isEmpty() )
      {
         testDataSourcePlugins.addAll( test.testDataSourcePlugins );
      }
   }

   @Override
   public int compareTo( Test other )
   {
      int lhs = ( priority == null ) ? 0 : priority;
      int rhs = ( other.priority == null ) ? 0 : other.priority;
      return lhs - rhs;
      // return ( priorityComparision == 0 ) ? name.compareTo( other.name ) : priorityComparision;
   }
}
