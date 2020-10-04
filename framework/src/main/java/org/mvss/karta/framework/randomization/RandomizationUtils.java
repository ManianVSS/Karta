package org.mvss.karta.framework.randomization;

import java.util.ArrayList;
import java.util.Random;

import org.mvss.karta.framework.utils.DataUtils;

public class RandomizationUtils
{

   public static <E extends ObjectWithChance> boolean checkForProbabilityCoverage( ArrayList<E> chanceObjects, float probabilityToCover )
   {
      return getMissingProbabilityCoverage( chanceObjects, probabilityToCover ) == 0;
   }

   public static <E extends ObjectWithChance> Float getMissingProbabilityCoverage( ArrayList<E> chanceObjects )
   {
      return getMissingProbabilityCoverage( chanceObjects, 100 );
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
         float probabilityOfOccurrence = chanceObject.getProbabilityOfOccurrence();

         if ( ( probabilityOfOccurrence == 0 ) || !DataUtils.inRange( probabilityOfOccurrence, 0, probabilityToCover ) )
         {
            return null;
         }

         probabilityNotCovered -= chanceObject.getProbabilityOfOccurrence();

         if ( !ignoreOverflow && ( probabilityNotCovered < 0 ) )
         {
            return null;
         }
      }
      return probabilityNotCovered;
   }

   public static <E extends ObjectWithChance> ArrayList<E> generateNextComposition( Random random, ArrayList<E> objectsToChooseFrom )
   {
      ArrayList<E> variableMap = new ArrayList<E>();

      if ( ( objectsToChooseFrom == null ) || ( objectsToChooseFrom.isEmpty() ) )
      {
         return variableMap;
      }

      for ( E object : objectsToChooseFrom )
      {
         float probabilityOfOccurrence = object.getProbabilityOfOccurrence();

         if ( ( probabilityOfOccurrence <= 0 ) || ( probabilityOfOccurrence > 100 ) )
         {
            continue;
         }

         if ( ( probabilityOfOccurrence == 100 ) || random.nextInt( 100 ) <= probabilityOfOccurrence )
         {
            variableMap.add( object );
         }
      }

      return variableMap;
   }

   public static <E extends ObjectWithChance> E generateNextMutexComposition( Random random, ArrayList<E> objectsToChooseFrom )
   {
      if ( ( objectsToChooseFrom == null ) || ( objectsToChooseFrom.isEmpty() ) )
      {
         return null;
      }

      float probabilityNotCovered = 100;
      int randomChance = random.nextInt( 100 ) + 1;
      boolean alreadyPicked = false;
      E returnValue = null;

      for ( E object : objectsToChooseFrom )
      {
         float probabilityOfOccurrence = object.getProbabilityOfOccurrence();

         if ( probabilityOfOccurrence < 0 )
         {
            return null;
         }

         float currProbRevCumulative = probabilityNotCovered - Math.min( 100, probabilityOfOccurrence );

         if ( !alreadyPicked && ( randomChance <= probabilityNotCovered ) && ( randomChance >= currProbRevCumulative ) )
         {
            returnValue = object;
            alreadyPicked = true;
         }
         probabilityNotCovered = currProbRevCumulative;
      }

      return ( probabilityNotCovered == 0 ) ? returnValue : null;
   }
}
