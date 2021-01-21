package org.mvss.karta.framework.randomization;

/**
 * Enumeration of various object generation rule types
 * 
 * @author Manian
 */
public enum ObjectGenerationRuleType
{
   /**
    * Binary data with length range
    */
   BINARY_DATA_RANGE,
   /**
    * String with a range
    */
   STRING_RANGE,
   /**
    * Integer value range
    */
   INTEGER_RANGE,
   /**
    * Long value range
    */
   LONG_RANGE,
   /**
    * Random boolean value true/false
    */
   BOOLEAN,
   /**
    * Random UUID in standard String format
    */
   UUID,
   /**
    * Generate object with fields with each field having an individual probability of occurrence
    */
   OBJECT_RULE,
   /**
    * 
    */
   /**
    * Generate a object from an mutually exclusive object rule selected by their probabilities.
    * Can be used to generate random values or field for parent rule.
    */
   MUTEX_OBJECT_RULE_VALUE,
   /**
    * The list of values to choose from
    */
   VALUES
}
