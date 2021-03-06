package org.mvss.karta.framework.randomization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.mvss.karta.framework.chaos.Chaos;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.DataUtils;

/**
 * Utility class for chance/probability based selection of objects
 * 
 * @author Manian
 */
public class RandomizationUtils
{
   /**
    * Check if the collection of objects with chance cover all possibilities (1.0f)
    * 
    * @param <E>
    * @param chanceObjects
    * @return
    */
   public static <E extends ObjectWithChance> boolean checkForProbabilityCoverage( Collection<E> chanceObjects )
   {
      return checkForProbabilityCoverage( chanceObjects, 1.0f );
   }

   /**
    * Check if the collection of objects with chance cover specified probability value.
    * 
    * @param <E>
    * @param chanceObjects
    * @param probabilityToCover
    * @return
    */
   public static <E extends ObjectWithChance> boolean checkForProbabilityCoverage( Collection<E> chanceObjects, float probabilityToCover )
   {
      return getMissingProbabilityCoverage( chanceObjects, probabilityToCover ) == 0;
   }

   /**
    * Evaluates the missing probability(assuming total to be 1.0f) from a collection of objects with chance.
    * 
    * @param <E>
    * @param chanceObjects
    * @return
    */
   public static <E extends ObjectWithChance> Float getMissingProbabilityCoverage( Collection<E> chanceObjects )
   {
      return getMissingProbabilityCoverage( chanceObjects, 1 );
   }

   /**
    * Evaluates the missing probability from a collection of objects with chance for the probability to cover.
    * Returns null for over shooting
    * 
    * @param <E>
    * @param chanceObjects
    * @param probabilityToCover
    * @return
    */
   public static <E extends ObjectWithChance> Float getMissingProbabilityCoverage( Collection<E> chanceObjects, float probabilityToCover )
   {
      return getMissingProbabilityCoverage( chanceObjects, probabilityToCover, false );
   }

   /**
    * Evaluates the missing probability from a collection of objects with chance for the probability to cover.
    * ignoreOverflow if set to true won't return null on over shooting probability to cover.
    * 
    * @param <E>
    * @param chanceObjects
    * @param probabilityToCover
    * @param ignoreOverflow
    * @return
    */
   public static <E extends ObjectWithChance> Float getMissingProbabilityCoverage( Collection<E> chanceObjects, float probabilityToCover, boolean ignoreOverflow )
   {
      float probabilityNotCovered = probabilityToCover;
      for ( ObjectWithChance chanceObject : chanceObjects )
      {
         float probability = chanceObject.getProbability();

         if ( !DataUtils.inRange( probability, 0, probabilityToCover, false, true ) )
         {
            return null;
         }

         probabilityNotCovered = probabilityNotCovered - chanceObject.getProbability();

         if ( !ignoreOverflow && ( probabilityNotCovered < 0 ) )
         {
            return null;
         }
      }

      return (float) ( ( (long) ( probabilityNotCovered * 1000000.f ) ) / 1000000l );
   }

   /**
    * Compose a ArrayList of objects selected from a collection of objects with individual probability of occurrences.
    * 
    * @param <E>
    * @param random
    * @param objectsToChooseFrom
    * @return
    */
   public static <E extends ObjectWithChance> ArrayList<E> generateNextComposition( Random random, Collection<E> objectsToChooseFrom )
   {
      ArrayList<E> chosenObjects = new ArrayList<E>();

      if ( ( objectsToChooseFrom == null ) || ( objectsToChooseFrom.isEmpty() ) )
      {
         return chosenObjects;
      }

      for ( E object : objectsToChooseFrom )
      {
         float probability = object.getProbability();

         if ( !DataUtils.inRange( probability, 0, 1 ) )
         {
            continue;
         }

         float randomChance = ( 1 + random.nextInt( 1000000 ) ) / 1000000.0f;

         if ( ( probability == 1 ) || ( randomChance <= probability ) )
         {
            chosenObjects.add( object );
         }
      }

      return chosenObjects;
   }

   /**
    * Select one object from a collection of objects with chance taking into consideration their probability of occurrence.
    * The sum of probabilities for the objects with chance should be 1.0f else null is returned.
    * 
    * @param <E>
    * @param random
    * @param objectsToChooseFrom
    * @return
    */
   public static <E extends ObjectWithChance> E generateNextMutexComposition( Random random, Collection<E> objectsToChooseFrom )
   {
      if ( ( objectsToChooseFrom == null ) || ( objectsToChooseFrom.isEmpty() ) )
      {
         return null;
      }

      // TODO: Check for consistency in probability
      float probabilityCovered = 0;
      float randomChance = ( 1 + random.nextInt( 1000000 ) ) / 1000000.0f;

      boolean alreadyPicked = false;
      E returnValue = null;

      for ( E object : objectsToChooseFrom )
      {
         float probability = object.getProbability();

         if ( !DataUtils.inRange( probability, 0, 1 ) )
         {
            return null;
         }

         if ( !alreadyPicked && DataUtils.inRange( randomChance, probabilityCovered, probabilityCovered + probability ) )
         {
            returnValue = object;
            alreadyPicked = true;
         }

         probabilityCovered += probability;
      }

      if ( !( Math.round( probabilityCovered ) == 1.0f ) )
      {
         return null;
      }

      return returnValue;
   }

   /**
    * Randomly discard few items by count from list using the provided randomizer.
    * 
    * @param <T>
    * @param random
    * @param items
    * @param count
    * @return
    */
   public static <T> ArrayList<T> discardListItems( Random random, Collection<T> items, int count )
   {
      ArrayList<T> returnList = null;

      if ( items != null )
      {
         if ( count >= items.size() )
         {
            returnList = new ArrayList<T>();
         }
         else
         {
            returnList = new ArrayList<T>( items );
            if ( count > 0 )
            {
               for ( int dicardedItems = 0; dicardedItems < count; dicardedItems++ )
               {
                  returnList.remove( random.nextInt( returnList.size() ) );
               }
            }
         }
      }

      return returnList;
   }

   /**
    * Randomly select few items by count from list using the provided randomizer.
    * 
    * @param <T>
    * @param random
    * @param items
    * @param count
    * @return
    */
   public static <T> ArrayList<T> selectListItems( Random random, Collection<T> items, int count )
   {
      ArrayList<T> returnList = null;

      if ( items != null )
      {
         if ( count <= 0 )
         {
            returnList = new ArrayList<T>();
         }
         else
         {
            returnList = new ArrayList<T>( items );

            if ( count < items.size() )
            {
               ArrayList<T> selectedList = new ArrayList<T>();
               for ( int selected = 0; selected < count; selected++ )
               {
                  selectedList.add( returnList.remove( random.nextInt( returnList.size() ) ) );
               }
               returnList = selectedList;
            }
         }
      }

      return returnList;
   }

   /**
    * Randomly select few items by percentage from list using the provided randomizer.
    * 
    * @param <T>
    * @param random
    * @param items
    * @param percentage
    * @return
    */
   public static <T> ArrayList<T> selectByPercentage( Random random, Collection<T> items, float percentage )
   {
      if ( items == null )
      {
         return null;
      }

      int selectCount = (int) Math.ceil( items.size() * percentage / 100 );
      return selectListItems( random, items, selectCount );
   }

   /**
    * Randomly select few items by max percentage(0-maxPercentage selected randomly) from list using the provided randomizer.
    * 
    * @param <T>
    * @param random
    * @param items
    * @param maxPercentage
    * @return
    */
   public static <T> ArrayList<T> selectByMaxPercentage( Random random, Collection<T> items, float maxPercentage )
   {
      if ( items == null )
      {
         return null;
      }

      float percentage = maxPercentage * ( ( 1 + random.nextInt( 1000000 ) ) / 1000000.0f );

      int selectCount = (int) Math.ceil( items.size() * percentage / 100 );
      return selectListItems( random, items, selectCount );
   }

   /**
    * Randomly select few items by the chaos level and unit using the provided randomizer.
    * 
    * @param <T>
    * @param random
    * @param items
    * @param chaos
    * @return
    */
   public static <T> Collection<T> selectByChaos( Random random, Collection<T> items, Chaos chaos )
   {
      if ( ( items == null ) || items.isEmpty() )
      {
         return items;
      }

      switch ( chaos.getChaosUnit() )
      {
         case CONSTANT:
            return selectListItems( random, items, (int) Math.ceil( chaos.getChaosLevel() ) );
         case ALL_BUT_ONE:
            return discardListItems( random, items, 1 );
         case MAX_PERCENTAGE:
            return selectByMaxPercentage( random, items, chaos.getChaosLevel() );
         case PERCENTAGE:
            return selectByPercentage( random, items, chaos.getChaosLevel() );
         default:
            return items;
      }
   }

   /**
    * Randomly generate an alpha numeric string of specified length.
    * 
    * @param random
    * @param length
    * @return
    */
   public static String randomAlphaNumericString( Random random, int length )
   {
      String returnValue = Constants.EMPTY_STRING;

      for ( int i = 0; i < length; i++ )
      {
         int randomDigit = random.nextInt( 62 );

         if ( randomDigit < 10 )
         {
            returnValue = returnValue + (char) ( '0' + randomDigit );
         }
         else if ( randomDigit < 36 )
         {
            returnValue = returnValue + (char) ( 'A' + ( randomDigit - 10 ) );
         }
         else
         {
            returnValue = returnValue + (char) ( 'a' + ( randomDigit - 36 ) );
         }

      }
      return returnValue;
   }

}
