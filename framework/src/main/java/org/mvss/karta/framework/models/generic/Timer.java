package org.mvss.karta.framework.models.generic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

/**
 * This class can store time taken for activities.
 *
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class Timer implements Serializable, Comparable<Timer> {
    private static final long serialVersionUID = 1L;

    private volatile Instant startTime;
    private volatile Instant endTime;

    public synchronized void start() {
        startTime = Instant.now();
        endTime = startTime;
    }

    public synchronized void stop() {
        endTime = Instant.now();

        if (startTime == null) {
            startTime = endTime;
        }
    }

    @JsonIgnore
    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }

    @Override
    public int compareTo(Timer otherTimer) {
        if (startTime == null) {
            return (otherTimer.startTime == null) ? 0 : 1;
        } else {
            if (otherTimer.startTime == null) {
                return -1;
            } else {
                int startTimeCompare = startTime.compareTo(otherTimer.startTime);

                if (startTimeCompare == 0) {
                    return getDuration().compareTo(otherTimer.getDuration());
                } else {
                    return startTimeCompare;
                }
            }
        }
    }
}
