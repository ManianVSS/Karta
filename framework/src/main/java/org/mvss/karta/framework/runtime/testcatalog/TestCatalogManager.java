package org.mvss.karta.framework.runtime.testcatalog;

import java.util.ArrayList;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TestCatalogManager
{
   private TestCategory testCatalog = new TestCategory();

   public void mergeWithCatalog( String sourceArchive, TestCategory updatesToRootCategory )
   {
      updatesToRootCategory.propogateSourceArchive( sourceArchive, updatesToRootCategory.getFeatureSourceParserPlugin(), updatesToRootCategory.getFeatureSourceParserPlugin(), updatesToRootCategory.getTestDataSourcePlugins() );
      testCatalog.mergeWithTestCategory( updatesToRootCategory );
   }

   public void mergeWithCatalog( String sourceArchive )
   {

   }

   public void mergeSubCategory( String sourceArchive, TestCategory testCategoryToMerge )
   {
      testCategoryToMerge.propogateSourceArchive( sourceArchive, testCategoryToMerge.getFeatureSourceParserPlugin(), testCategoryToMerge.getFeatureSourceParserPlugin(), testCategoryToMerge.getTestDataSourcePlugins() );
      testCatalog.mergeTestCategory( testCategoryToMerge );
   }

   public void addTest( String sourceArchive, Test testToMerge )
   {
      testCatalog.addTest( testToMerge );
   }

   public ArrayList<Test> filterTestsByTag( String tag )
   {
      ArrayList<Test> outputFilteredTests = new ArrayList<Test>();
      testCatalog.filterTestsByTag( tag, outputFilteredTests );
      return outputFilteredTests;
   }
}
