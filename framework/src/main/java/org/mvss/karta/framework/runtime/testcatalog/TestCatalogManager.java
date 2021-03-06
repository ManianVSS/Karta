package org.mvss.karta.framework.runtime.testcatalog;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.ClassPathLoaderUtils;
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
   private static ObjectMapper yamlObjectMapper = ParserUtils.getYamlObjectMapper();

   private TestCategory        testCatalog      = new TestCategory();

   public void mergeWithCatalog( TestCategory updatesToRootCategory )
   {
      updatesToRootCategory.propogateAttributes( null, updatesToRootCategory.getFeatureSourceParserPlugin(), updatesToRootCategory.getFeatureSourceParserPlugin(), updatesToRootCategory.getTestDataSourcePlugins(), updatesToRootCategory
               .getThreadGroup(), updatesToRootCategory.getTags() );
      testCatalog.mergeWithTestCategory( updatesToRootCategory );
   }

   public void mergeWithCatalog( String sourceArchive ) throws Throwable
   {
      InputStream fileStream = null;

      if ( StringUtils.isNotBlank( sourceArchive ) )
      {
         ClassLoader loader = DynamicClassLoader.getClassLoaderForJar( sourceArchive );

         if ( loader == null )
         {
            return;
         }
         fileStream = loader.getResourceAsStream( Constants.TEST_CATALOG_FRAGMENT_FILE_NAME );
      }
      else
      {
         fileStream = ClassPathLoaderUtils.getFileStream( Constants.TEST_CATALOG_FRAGMENT_FILE_NAME );
      }

      if ( fileStream == null )
      {
         return;
      }

      TestCategory updatesToRootCategory = yamlObjectMapper.readValue( IOUtils.toString( fileStream, Charset.defaultCharset() ), TestCategory.class );
      updatesToRootCategory.propogateAttributes( sourceArchive, updatesToRootCategory.getFeatureSourceParserPlugin(), updatesToRootCategory.getFeatureSourceParserPlugin(), updatesToRootCategory.getTestDataSourcePlugins(), updatesToRootCategory
               .getThreadGroup(), updatesToRootCategory.getTags() );
      testCatalog.mergeWithTestCategory( updatesToRootCategory );
   }

   public void mergeRepositoryDirectoryIntoCatalog( File repositoryDirectory )
   {
      try
      {
         mergeWithCatalog( (String) null );
      }
      catch ( Throwable t )
      {
         log.error( "Failed to load test catalog from classpath", t );
      }

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
      testCatalog.mergeTest( testToMerge );
   }

   public void addTest( String sourceArchive, Test testToMerge )
   {
      testCatalog.mergeTest( testToMerge );
   }

   private static ConcurrentHashMap<String, Pattern> patternsMap = new ConcurrentHashMap<String, Pattern>();

   public synchronized ArrayList<Test> filterTestsByTag( HashSet<String> tags )
   {
      ArrayList<Test> outputFilteredTests = new ArrayList<Test>();
      HashSet<Pattern> tagPatterns = new HashSet<Pattern>();

      for ( String tag : tags )
      {
         if ( !patternsMap.contains( tag ) )
         {
            patternsMap.put( tag, Pattern.compile( tag ) );
         }
         Pattern tagPattern = patternsMap.get( tag );
         tagPatterns.add( tagPattern );
      }
      testCatalog.filterTestsByTag( outputFilteredTests, tagPatterns );
      return outputFilteredTests;
   }
}
