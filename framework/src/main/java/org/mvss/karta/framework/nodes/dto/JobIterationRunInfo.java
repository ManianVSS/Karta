package org.mvss.karta.framework.nodes.dto;

import lombok.*;
import org.mvss.karta.dependencyinjection.TestProperties;
import org.mvss.karta.framework.models.run.RunInfo;
import org.mvss.karta.framework.models.test.TestJob;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class JobIterationRunInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private RunInfo runInfo;
    private String featureName;
    private TestProperties testProperties;
    private TestJob testJob;
    @Builder.Default
    private int iterationIndex = -1;
}
