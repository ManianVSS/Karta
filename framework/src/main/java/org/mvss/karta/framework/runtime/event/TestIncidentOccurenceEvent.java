package org.mvss.karta.framework.runtime.event;

import java.util.Date;
import java.util.UUID;

import org.mvss.karta.framework.core.TestIncident;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
public class TestIncidentOccurenceEvent extends Event
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private TestIncident      incident;
   private String            runName;
   private String            featureName;
   private String            scenarioName;
   private String            stepIdentifier;

   private int               iteration;

   public TestIncidentOccurenceEvent( String runName, String featureName, Integer iteration, String scenarioName, String stepIdentifier, TestIncident incident )
   {
      super( StandardEventsTypes.FEATURE_SETUP_STEP_COMPLETE_EVENT, runName );
      this.incident = incident;
      this.featureName = featureName;
      this.iteration = iteration;
      this.scenarioName = scenarioName;
      this.stepIdentifier = stepIdentifier;
   }

   @Builder
   public TestIncidentOccurenceEvent( String runName, UUID id, Date timeOfOccurrence, String featureName, Integer iteration, String scenarioName, String stepIdentifier, TestIncident incident )
   {
      super( StandardEventsTypes.FEATURE_SETUP_STEP_COMPLETE_EVENT, runName, id, timeOfOccurrence );
      this.incident = incident;
      this.incident = incident;
      this.featureName = featureName;
      this.iteration = iteration;
      this.scenarioName = scenarioName;
      this.stepIdentifier = stepIdentifier;
   }
}
