package org.mvss.karta.framework.runtime.testcatalog;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.DynamicClassLoader;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Getter
@NoArgsConstructor
public class TestCatalogManager
{
   private static ObjectMapper objectMapper = ParserUtils.getObjectMapper();

   private TestCategory        testCatalog  = new TestCategory();

   public void mergeWithCatalog( TestCategory updatesToRootCategory )
   {
      testCatalog.mergeWithTestCategory( updatesToRootCategory );
   }

   public void mergeWithCatalog( String sourceArchive ) throws Throwable
   {
      ClassLoader loader = DynamicClassLoader.getClassLoaderForJar( sourceArchive );

      if ( loader == null )
      {
         return;
      }

      InputStream fileStream = loader.getResourceAsStream( Constants.TEST_CATALOG_FILE_NAME );

      if ( fileStream == null )
      {
         return;
      }

      TestCategory updatesToRootCategory = objectMapper.readValue( IOUtils.toString( fileStream, Charset.defaultCharset() ), TestCategory.class );
      updatesToRootCategory.propogateSourceArchive( sourceArchive, updatesToRootCategory.getFeatureSourceParserPlugin(), updatesToRootCategory.getFeatureSourceParserPlugin(), updatesToRootCategory.getTestDataSourcePlugins() );
      testCatalog.mergeWithTestCategory( updatesToRootCategory );
   }

   public void mergeRepositoryDirectoryIntoCatalog( File repositoryDirectory )
   {
      for ( File jarFile : FileUtils.listFiles( repositoryDirectory, Constants.jarExtention, true ) )
      {
         try
         {
            mergeWithCatalog( jarFile.getAbsolutePath() );
         }
         catch ( Throwable t )
         {
            log.error( "Failed to load test repository jar: " + jarFile.getAbsolutePath(), t );
         }
      }
   }

   public void mergeSubCategory( TestCategory testCategoryToMerge )
   {
      testCatalog.mergeTestCategory( testCategoryToMerge );
   }

   public void addTest( Test testToMerge )
   {
      testCatalog.addTest( testToMerge );
   }

   public void addTest( String sourceArchive, Test testToMerge )
   {
      testCatalog.addTest( testToMerge );
   }

   public ArrayList<Test> filterTestsByTag( String... tags )
   {
      ArrayList<Test> outputFilteredTests = new ArrayList<Test>();
      testCatalog.filterTestsByTag( outputFilteredTests, tags );
      return outputFilteredTests;
   }
}