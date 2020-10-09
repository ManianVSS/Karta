package org.mvss.karta.framework.randomization;

import java.util.ArrayList;
import java.util.Random;

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

         probabilityNotCovered -= chanceObject.getProbability();

         if ( !ignoreOverflow && ( probabilityNotCovered < 0 ) )
         {
            return null;
         }
      }
      return probabilityNotCovered;
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
}
