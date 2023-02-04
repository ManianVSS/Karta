package org.mvss.karta.framework.models.chaos;

/**
 * The unit of chaos. Meaning is subjective to the actual chaos type.
 *
 * @author Manian
 * @see Chaos
 */
public enum ChaosUnit {
    /**
     * Percentage of chaos.
     */
    PERCENTAGE,
    /**
     * Maximum chaos percentage. Actual chaos will vary between 0 and value provided.
     */
    MAX_PERCENTAGE,
    /**
     * Constant chaos denoted by a float. Can be converted to an integer if a whole number.
     */
    CONSTANT,
    /**
     * Chaos on all but one instance. Useful to denote chaos to be applied to all but one unit in a multiple replica.
     */
    ALL_BUT_ONE
}
