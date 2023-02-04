package org.mvss.karta.framework.models.result;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.mvss.karta.framework.models.generic.SerializableKVP;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * The result of a test-job iteration
 *
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestJobResult implements Serializable, Comparable<TestJobResult> {
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private long iterationIndex = 0;

    @Builder.Default
    private Date startTime = new Date();

    private Date endTime;

    @Builder.Default
    private boolean successful = true;

    @Builder.Default
    private boolean error = false;

    @Builder.Default
    private ArrayList<SerializableKVP<String, StepResult>> stepResults = new ArrayList<>();

    @Override
    public int compareTo(TestJobResult other) {
        return Long.compare(iterationIndex, other.iterationIndex);
    }

    @JsonIgnore
    public boolean isPassed() {
        return successful && !error;
    }

    /**
     * Converts events and other objects received from remote execution to appropriate subclass
     */
    public void processRemoteResults() {
        for (SerializableKVP<String, StepResult> setupResult : stepResults) {
            setupResult.getValue().processRemoteResults();
        }
    }
}
