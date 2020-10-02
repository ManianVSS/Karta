package org.mvss.karta.framework.randomization;

import java.util.ArrayList;
import java.util.Random;

public class RandomizationUtils
{
   public static <E extends ObjectWithChance> ArrayList<E> generateNextComposition( Random random, ArrayList<E> objectsToChooseFrom )
   {
      ArrayList<E> variableMap = new ArrayList<E>();

      if ( ( objectsToChooseFrom == null ) || ( objectsToChooseFrom.isEmpty() ) )
      {
         return variableMap;
      }

      for ( E object : objectsToChooseFrom )
      {
         int probabilityOfOccurrence = object.getProbabilityOfOccurrence();

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

      int probabilityNotCovered = 100;
      int randomChance = random.nextInt( 100 ) + 1;
      boolean alreadyPicked = false;
      E returnValue = null;

      for ( E object : objectsToChooseFrom )
      {
         int probabilityOfOccurrence = object.getProbabilityOfOccurrence();

         if ( probabilityOfOccurrence < 0 )
         {
            return null;
         }

         int currProbRevCumulative = probabilityNotCovered - Math.min( 100, probabilityOfOccurrence );

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
