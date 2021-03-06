package org.mvss.karta.framework.randomization;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * An ObjectWithChange implementation to hold generic object to map a probability with.
 * 
 * @author Manian
 * @param <T>
 */
@Getter
@AllArgsConstructor
public class GenericObjectWithChance<T> implements ObjectWithChance
{
   public T     object;
   public float probability = 1.0f;

   /**
    * Converts a map of object to probability to a list of GenericObjectWithChance objects.
    * 
    * @param <T>
    * @param objectProbabiltyMap
    * @return
    */
   public static <T> ArrayList<GenericObjectWithChance<T>> toList( HashMap<T, Float> objectProbabiltyMap )
   {
      ArrayList<GenericObjectWithChance<T>> convertedList = new ArrayList<GenericObjectWithChance<T>>();

      if ( objectProbabiltyMap != null )
      {
         objectProbabiltyMap.forEach( ( object, probability ) -> convertedList.add( new GenericObjectWithChance<T>( object, probability ) ) );
      }
      return convertedList;
   }

   /**
    * Add a new mapping of object and probability to a list of GenericObjectWithChance
    * 
    * @param <T>
    * @param object
    * @param probability
    * @param objectsWithChance
    */
   public static <T> void addToList( T object, float probability, Collection<GenericObjectWithChance<T>> objectsWithChance )
   {
      if ( objectsWithChance != null )
      {
         objectsWithChance.add( new GenericObjectWithChance<T>( object, probability ) );
      }
   }

   /**
    * Extract objects form list of GenericObjectWithChance into another list
    * 
    * @param <T>
    * @param objectsWithChance
    * @return
    */
   public static <T> ArrayList<T> extractObjects( ArrayList<GenericObjectWithChance<T>> objectsWithChance )
   {
      ArrayList<T> extractedObjects = new ArrayList<T>();

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
