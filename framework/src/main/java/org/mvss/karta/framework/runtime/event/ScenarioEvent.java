package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.TestScenario;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.DataUtils;
import org.mvss.karta.framework.utils.ParserUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode( callSuper = true )
@ToString
@NoArgsConstructor
public abstract class ScenarioEvent extends FeatureEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public ScenarioEvent( Event event )
   {
      super( event );
      parameters.put( Constants.ITERATION_NUMBER, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.ITERATION_NUMBER ), Long.class ) );
      parameters.put( Constants.SCENARIO, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.SCENARIO ), TestScenario.class ) );
      parameters.put( Constants.SCENARIO_NAME, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.SCENARIO_NAME ), String.class ) );
   }

   public ScenarioEvent( String eventType, String runName, String featureName, long iterationNumber, TestScenario scenario )
   {
      super( eventType, runName, featureName );
      this.parameters.put( Constants.ITERATION_NUMBER, iterationNumber );
      this.parameters.put( Constants.SCENARIO, scenario );
      this.parameters.put( Constants.SCENARIO_NAME, scenario.getName() );
   }

   public ScenarioEvent( String eventType, String runName, String featureName, long iterationNumber, String scenarioName )
   {
      super( eventType, runName, featureName );
      this.parameters.put( Constants.ITERATION_NUMBER, iterationNumber );
      this.parameters.put( Constants.SCENARIO, TestScenario.builder().name( scenarioName ).build() );
      this.parameters.put( Constants.SCENARIO_NAME, scenarioName );
   }

   @JsonIgnore
   public long getIterationNumber()
   {
      return DataUtils.serializableToLong( parameters.get( Constants.ITERATION_NUMBER ), -1 );
   }

   @JsonIgnore
   public TestScenario getScenario()
   {
      return (TestScenario) parameters.get( Constants.SCENARIO );
   }

   @JsonIgnore
   public String getScenarioName()
   {
      return parameters.get( Constants.SCENARIO_NAME ).toString();
   }
}
