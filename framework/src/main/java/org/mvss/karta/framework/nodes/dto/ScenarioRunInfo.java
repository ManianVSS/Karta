package org.mvss.karta.framework.nodes.dto;

import lombok.*;
import org.mvss.karta.framework.core.PreparedScenario;
import org.mvss.karta.framework.runtime.RunInfo;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder( toBuilder = true )
public class ScenarioRunInfo implements Serializable
{
   private static final long serialVersionUID = 1L;

   private RunInfo          runInfo;
   private String           featureName;
   private int              iterationIndex          = -1;
   private PreparedScenario preparedScenario;
   private long             scenarioIterationNumber = -1;
}
