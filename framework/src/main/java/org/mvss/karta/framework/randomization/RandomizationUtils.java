package org.mvss.karta.framework.randomization;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.mvss.karta.framework.chaos.ChaosUnit;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.DataUtils;

public class RandomizationUtils
{
   public static <E extends ObjectWithChance> boolean checkForProbabilityCoverage( ArrayList<E> chanceObjects )
   {
      return checkForProbabilityCoverage( chanceObjects, 1.0f );
   }

   public static <E extends ObjectWithChance> boolean checkForProbabilityCoverage( ArrayList<E> chanceObjects, float probabilityToCover )
   {
      return getMissingProbabilityCoverage( chanceObjects, probabilityToCover ) == 0;
   }

   public static <E extends ObjectWithChance> Float getMissingProbabilityCoverage( ArrayList<E> chanceObjects )
   {
      return getMissingProbabilityCoverage( chanceObjects, 1 );
   }

   public static <E extends ObjectWithChance> Float getMissingProbabilityCoverage( ArrayList<E> chanceObjects, float probabilityToCover )
   {
      return getMissingProbabilityCoverage( chanceObjects, probabilityToCover, false );
   }

   public static <E extends ObjectWithChance> Float getMissingProbabilityCoverage( ArrayList<E> chanceObjects, float probabilityToCover, boolean ignoreOverflow )
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

   public static <E extends ObjectWithChance> ArrayList<E> generateNextComposition( Random random, ArrayList<E> objectsToChooseFrom )
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

   public static <E extends ObjectWithChance> E generateNextMutexComposition( Random random, ArrayList<E> objectsToChooseFrom )
   {
      if ( ( objectsToChooseFrom == null ) || ( objectsToChooseFrom.isEmpty() ) )
      {
         return null;
      }

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

   public static <T> List<T> discardListItems( Random random, List<T> items, int count )
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

   public static <T> List<T> selectListItems( Random random, List<T> items, int count )
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

   public static <T> List<T> selectByPercentage( Random random, List<T> items, float percentage )
   {
      if ( items == null )
      {
         return null;
      }

      int selectCount = (int) Math.ceil( items.size() * percentage / 100 );
      return selectListItems( random, items, selectCount );
   }

   public static <T> List<T> selectByMaxPercentage( Random random, List<T> items, float maxPercentage )
   {
      if ( items == null )
      {
         return null;
      }

      float percentage = maxPercentage * ( ( 1 + random.nextInt( 1000000 ) ) / 1000000.0f );

      int selectCount = (int) Math.ceil( items.size() * percentage / 100 );
      return selectListItems( random, items, selectCount );
   }

   public static <T> List<T> selectByChaos( Random random, List<T> items, float chaosLevel, ChaosUnit chaosUnit )
   {
      if ( ( items == null ) || items.isEmpty() )
      {
         return items;
      }

      switch ( chaosUnit )
      {
         case CONSTANT:
            return selectListItems( random, items, (int) Math.ceil( chaosLevel ) );
         case ALL_BUT_ONE:
            return discardListItems( random, items, 1 );
         case MAX_PERCENTAGE:
            return selectByMaxPercentage( random, items, chaosLevel );
         case PERCENTAGE:
            return selectByPercentage( random, items, chaosLevel );
         default:
            return items;
      }
   }

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
