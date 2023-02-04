package org.mvss.karta.framework.models.test;

import lombok.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

/**
 * This class describes a test incident which might occur when running a test step.
 *
 * @author Manian
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestIncident implements Serializable {
    private static final long serialVersionUID = 1L;

    private HashSet<String> tags;
    private String message;
    private Throwable thrownCause;

    @Builder.Default
    private Date timeOfOccurrence = new Date();

    public TestIncident(String message, Throwable thrownCause, String... tags) {
        this.message = message;
        this.thrownCause = thrownCause;

        if (tags != null) {
            this.tags = new HashSet<>();
            Collections.addAll(this.tags, tags);
        }
    }
}
