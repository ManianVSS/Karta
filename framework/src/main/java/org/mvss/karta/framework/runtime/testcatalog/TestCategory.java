package org.mvss.karta.framework.runtime.testcatalog;

import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCategory implements Serializable
{
   private static final long serialVersionUID = 1L;

   private String name;
   private String description;

   @Builder.Default
   private HashSet<String> tags = new HashSet<>();

   private String featureSourceParser;
   private String stepRunner;

   @Builder.Default
   private HashSet<String> testDataSources = new HashSet<>();

   @Builder.Default
   private ArrayList<TestCategory> subCategories = new ArrayList<>();

   @Builder.Default
   private ArrayList<Test> tests = new ArrayList<>();

   private String threadGroup;

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

   private static ConcurrentHashMap<Pattern, ConcurrentHashMap<String, Matcher>> patternMatcherMap = new ConcurrentHashMap<>();

   public synchronized void filterTestsByTag( ArrayList<Test> outputFilteredTests, HashSet<Pattern> tagPatterns )
   {
      for ( Pattern tagPattern : tagPatterns )
      {
         if ( !patternMatcherMap.containsKey( tagPattern ) )
         {
            patternMatcherMap.put( tagPattern, new ConcurrentHashMap<>() );
         }
         ConcurrentHashMap<String, Matcher> matcherMap = patternMatcherMap.get( tagPattern );
         for ( String tag : this.tags )
         {
            Matcher matcher = matcherMap.get( tag );
            if ( matcher == null )
            {
               matcherMap.put( tag, matcher = tagPattern.matcher( tag ) );
            }
            if ( matcher.matches() )
            {
               addAllTestsToList( outputFilteredTests );
               return;
            }
         }

         for ( Test test : tests )
         {
            for ( String tag : test.getTags() )
            {
               Matcher matcher = matcherMap.get( tag );
               if ( matcher == null )
               {
                  matcherMap.put( tag, matcher = tagPattern.matcher( tag ) );
               }
               if ( matcher.matches() )
               {
                  outputFilteredTests.add( test );
               }
            }
         }
      }

      for ( TestCategory subCategory : subCategories )
      {
         subCategory.filterTestsByTag( outputFilteredTests, tagPatterns );
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

   public void propagateAttributes( String sourceArchive, String inFeatureSourceParser, String srp, HashSet<String> inTestDataSources, String tg,
                                    HashSet<String> tags )
   {
      if ( StringUtils.isEmpty( featureSourceParser ) && StringUtils.isNotEmpty( inFeatureSourceParser ) )
      {
         featureSourceParser = inFeatureSourceParser;
      }

      if ( StringUtils.isEmpty( stepRunner ) && StringUtils.isNotEmpty( srp ) )
      {
         stepRunner = srp;
      }

      if ( inTestDataSources != null )
      {
         testDataSources.addAll( inTestDataSources );
      }

      if ( StringUtils.isEmpty( threadGroup ) && StringUtils.isNotEmpty( tg ) )
      {
         threadGroup = tg;
      }

      if ( tags != null )
      {
         this.tags.addAll( tags );
      }

      for ( TestCategory testCategory : subCategories )
      {
         testCategory.propagateAttributes( sourceArchive, featureSourceParser, stepRunner, testDataSources, threadGroup, tags );
      }

      for ( Test test : tests )
      {
         test.propagateAttributes( sourceArchive, featureSourceParser, stepRunner, testDataSources, threadGroup, tags );
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

      tags.addAll( testCategory.tags );

      if ( StringUtils.isEmpty( featureSourceParser ) && StringUtils.isNotEmpty( testCategory.featureSourceParser ) )
      {
         featureSourceParser = testCategory.featureSourceParser;
      }

      if ( StringUtils.isEmpty( stepRunner ) && StringUtils.isNotEmpty( testCategory.stepRunner ) )
      {
         stepRunner = testCategory.stepRunner;
      }

      if ( testDataSources.isEmpty() && !testCategory.testDataSources.isEmpty() )
      {
         testDataSources.addAll( testCategory.testDataSources );
      }

      for ( TestCategory testSubCatToAdd : testCategory.getSubCategories() )
      {
         mergeTestCategory( testSubCatToAdd );
      }

      for ( Test test : testCategory.getTests() )
      {
         mergeTest( test );
      }

      if ( StringUtils.isEmpty( threadGroup ) && StringUtils.isNotEmpty( testCategory.threadGroup ) )
      {
         threadGroup = testCategory.threadGroup;
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

   public void mergeTest( Test test )
   {
      if ( test == null )
      {
         return;
      }

      Test testToEdit = findTestByName( test.getName() );

      if ( testToEdit == null )
      {
         tests.add( test );
      }
      else
      {
         testToEdit.mergeWithTest( test );
      }
   }
}
