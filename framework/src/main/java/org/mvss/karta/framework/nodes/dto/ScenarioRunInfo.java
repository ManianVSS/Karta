package org.mvss.karta.framework.nodes.dto;

import lombok.*;
import org.mvss.karta.framework.models.run.RunInfo;
import org.mvss.karta.framework.models.test.PreparedScenario;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ScenarioRunInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private RunInfo runInfo;
    private String featureName;
    @Builder.Default
    private int iterationIndex = -1;
    private PreparedScenario preparedScenario;
    @Builder.Default
    private long scenarioIterationNumber = -1;
}
