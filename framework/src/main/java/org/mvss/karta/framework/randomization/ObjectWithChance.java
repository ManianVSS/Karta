package org.mvss.karta.framework.randomization;

/**
 * An object with a chance of occurrence.
 * This interface is used for randomly selecting objects from a list
 *
 * @author Manian
 * @see RandomizationUtils
 */
public interface ObjectWithChance
{
   /**
    * Method to be implemented to return the probability of occurrence for the object.
    * This could be same as the getter for a float type property "probability".
    *
    * @return float
    */
   float getProbability();
}
