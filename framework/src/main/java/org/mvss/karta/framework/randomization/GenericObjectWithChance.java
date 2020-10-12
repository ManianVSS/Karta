package org.mvss.karta.framework.randomization;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GenericObjectWithChance<T> implements ObjectWithChance
{
   public T     object;
   public float probability = 1.0f;

   public static <T> ArrayList<GenericObjectWithChance<T>> toList( HashMap<T, Float> objectProbabiltyMap )
   {
      ArrayList<GenericObjectWithChance<T>> convertedList = new ArrayList<GenericObjectWithChance<T>>();

      if ( objectProbabiltyMap != null )
      {
         objectProbabiltyMap.forEach( ( object, probability ) -> convertedList.add( new GenericObjectWithChance<T>( object, probability ) ) );
      }
      return convertedList;
   }

   public static <T> void addToList( T object, float probability, ArrayList<GenericObjectWithChance<T>> objectsWithChance )
   {
      if ( objectsWithChance != null )
      {
         objectsWithChance.add( new GenericObjectWithChance<T>( object, probability ) );
      }
   }

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
