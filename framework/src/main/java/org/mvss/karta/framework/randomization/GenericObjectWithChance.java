package org.mvss.karta.framework.randomization;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * An ObjectWithChange implementation to hold generic object to map a probability with.
 *
 * @param <T>
 * @author Manian
 */
@Getter
@AllArgsConstructor
public class GenericObjectWithChance<T> implements ObjectWithChance
{
   public T     object;
   public float probability = 1.0f;

   /**
    * Converts a map of object to probability to a list of GenericObjectWithChance objects.
    */
   public static <T> ArrayList<GenericObjectWithChance<T>> toList( HashMap<T, Float> objectProbabilityMap )
   {
      ArrayList<GenericObjectWithChance<T>> convertedList = new ArrayList<>();

      if ( objectProbabilityMap != null )
      {
         objectProbabilityMap.forEach( ( object, probability ) -> convertedList.add( new GenericObjectWithChance<>( object, probability ) ) );
      }
      return convertedList;
   }

   /**
    * Add a new mapping of object and probability to a list of GenericObjectWithChance
    */
   public static <T> void addToList( T object, float probability, Collection<GenericObjectWithChance<T>> objectsWithChance )
   {
      if ( objectsWithChance != null )
      {
         objectsWithChance.add( new GenericObjectWithChance<>( object, probability ) );
      }
   }

   /**
    * Extract objects form list of GenericObjectWithChance into another list
    */
   public static <T> ArrayList<T> extractObjects( ArrayList<GenericObjectWithChance<T>> objectsWithChance )
   {
      ArrayList<T> extractedObjects = new ArrayList<>();

      if ( objectsWithChance != null )
      {
         for ( GenericObjectWithChance<T> objectWithChance : objectsWithChance )
         {
            extractedObjects.add( objectWithChance.object );
         }
      }
      return extractedObjects;
   }

}
