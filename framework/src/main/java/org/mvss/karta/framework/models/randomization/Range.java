package org.mvss.karta.framework.models.randomization;

import lombok.*;

import java.io.Serializable;
import java.util.Random;

/**
 * A generic numeric range which can act as a range for numerical values
 *
 * @author Manian
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Range implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private long min = 0;
    private long max = Long.MAX_VALUE;

    /**
     * Select a random value in the range using the provided randomizer.
     *
     * @param random Random
     * @return long
     */
    public long getNext(Random random) {
        if (max < min) {
            long temp = max;
            max = min;
            min = temp;
        }
        if (max == min) {
            return min;
        }

        return min + Math.abs(random.nextLong()) % (max - min);
    }
}
