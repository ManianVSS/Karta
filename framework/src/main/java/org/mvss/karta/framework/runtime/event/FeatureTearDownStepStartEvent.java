package org.mvss.karta.framework.runtime.event;

import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.core.TestStep;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.Constants;
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
public class FeatureTearDownStepStartEvent extends FeatureEvent
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public FeatureTearDownStepStartEvent( Event event )
   {
      super( event );
      parameters.put( Constants.STEP, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.STEP ), TestStep.class ) );
   }

   public FeatureTearDownStepStartEvent( String runName, TestFeature feature, TestStep step )
   {
      super( StandardEventsTypes.FEATURE_TEARDOWN_STEP_START_EVENT, runName, feature );
      this.parameters.put( Constants.STEP, step );
   }

   @JsonIgnore
   public TestStep getStep()
   {
      return (TestStep) parameters.get( Constants.STEP );
   }
}
