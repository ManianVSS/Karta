package org.mvss.karta.framework.runtime.testcatalog;

import java.io.Serializable;
import java.util.ArrayList;
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
public class TestCategory implements Serializable
{

   /**
    * 
    */
   private static final long       serialVersionUID      = 1L;

   private String                  name;
   private String                  description;

   @Builder.Default
   private HashSet<String>         tags                  = new HashSet<String>();

   private String                  featureSourceParserPlugin;
   private String                  stepRunnerPlugin;

   @Builder.Default
   private HashSet<String>         testDataSourcePlugins = new HashSet<String>();

   @Builder.Default
   private ArrayList<Test>         tests                 = new ArrayList<Test>();

   @Builder.Default
   private ArrayList<TestCategory> subCategories         = new ArrayList<TestCategory>();

   public Test findTestByName( String name )
   {
      for ( Test test : tests )
      {
         if ( test.getName().contentEquals( name ) )
         {
            return test;
         }
      }
      return null;
   }

   public void filterTestsByTag( ArrayList<Test> outputFilteredTests, HashSet<String> tags )
   {
      for ( String tag : tags )
      {
         if ( this.tags.contains( tag ) )
         {
            addAllTestsToList( outputFilteredTests );
            return;
         }
      }

      for ( Test test : tests )
      {
         for ( String tag : tags )
         {
            if ( test.getTags().contains( tag ) )
            {
               outputFilteredTests.add( test );
            }
         }
      }

      for ( TestCategory subCategory : subCategories )
      {
         subCategory.filterTestsByTag( outputFilteredTests, tags );
      }

   }

   public void addAllTestsToList( ArrayList<Test> outputTests )
   {
      outputTests.addAll( tests );

      for ( TestCategory subCategory : subCategories )
      {
         subCategory.addAllTestsToList( outputTests );
      }
   }

   public TestCategory findTestCategoryByName( String name )
   {
      for ( TestCategory testCategory : subCategories )
      {
         if ( testCategory.getName().contentEquals( name ) )
         {
            return testCategory;
         }
      }
      return null;
   }

   public void propogateSourceArchive( String sourceArchive, String fspp, String srp, HashSet<String> tdsp )
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

      for ( TestCategory testCategory : subCategories )
      {
         testCategory.propogateSourceArchive( sourceArchive, featureSourceParserPlugin, stepRunnerPlugin, testDataSourcePlugins );
      }

      for ( Test test : tests )
      {
         test.propogateSourceArchive( sourceArchive, featureSourceParserPlugin, stepRunnerPlugin, testDataSourcePlugins );
      }
   }

   public void mergeWithTestCategory( TestCategory testCategory )
   {
      if ( testCategory == null )
      {
         return;
      }

      if ( StringUtils.isEmpty( name ) && StringUtils.isNotEmpty( testCategory.name ) )
      {
         name = testCategory.name;
      }

      if ( StringUtils.isEmpty( description ) && StringUtils.isNotEmpty( testCategory.description ) )
      {
         description = testCategory.description;
      }

      for ( String tag : testCategory.tags )
      {
         if ( !tags.contains( tag ) )
         {
            tags.add( tag );
         }
      }

      for ( TestCategory test : testCategory.getSubCategories() )
      {
         mergeTestCategory( test );
      }

      for ( Test test : testCategory.getTests() )
      {
         addTest( test );
      }
   }

   public void mergeTestCategory( TestCategory testCategory )
   {
      if ( testCategory == null )
      {
         return;
      }

      TestCategory existingTestCategory = findTestCategoryByName( testCategory.getName() );

      if ( existingTestCategory == null )
      {
         subCategories.add( testCategory );
      }
      else
      {
         existingTestCategory.mergeWithTestCategory( testCategory );
      }
   }

   public void addTest( Test test )
   {
      if ( test == null )
      {
         return;
      }

      if ( findTestByName( test.getName() ) == null )
      {
         tests.add( test );
      }
   }
}
