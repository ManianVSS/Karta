package org.mvss.karta.framework.runtime.testcatalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.ClassPathLoaderUtils;
import org.mvss.karta.framework.utils.DynamicClassLoader;
import org.mvss.karta.framework.utils.ParserUtils;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Log4j2
@Getter
@NoArgsConstructor
public class TestCatalogManager
{
   private static final ObjectMapper yamlObjectMapper = ParserUtils.getYamlObjectMapper();

   private final TestCategory testCatalog = new TestCategory();

   public void mergeWithCatalog( TestCategory updatesToRootCategory )
   {
      updatesToRootCategory.propagateAttributes( null, updatesToRootCategory.getFeatureSourceParser(), updatesToRootCategory.getFeatureSourceParser(),
               updatesToRootCategory.getTestDataSources(), updatesToRootCategory.getThreadGroup(), updatesToRootCategory.getTags() );
      testCatalog.mergeWithTestCategory( updatesToRootCategory );
   }

   public void mergeWithCatalog( String sourceArchive ) throws Throwable
   {
      InputStream fileStream;

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
      updatesToRootCategory.propagateAttributes( sourceArchive, updatesToRootCategory.getFeatureSourceParser(),
               updatesToRootCategory.getFeatureSourceParser(), updatesToRootCategory.getTestDataSources(), updatesToRootCategory.getThreadGroup(),
               updatesToRootCategory.getTags() );
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

      for ( File jarFile : FileUtils.listFiles( repositoryDirectory, Constants.jarExtension, true ) )
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

   private static final ConcurrentHashMap<String, Pattern> patternsMap = new ConcurrentHashMap<>();

   public synchronized ArrayList<Test> filterTestsByTag( HashSet<String> tags )
   {
      ArrayList<Test>  outputFilteredTests = new ArrayList<>();
      HashSet<Pattern> tagPatterns         = new HashSet<>();

      for ( String tag : tags )
      {
         if ( !patternsMap.containsKey( tag ) )
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
