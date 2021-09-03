package org.mvss.karta.framework.runtime.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.mvss.karta.framework.core.FeatureResult;
import org.mvss.karta.framework.core.TestFeature;
import org.mvss.karta.framework.enums.DataFormat;
import org.mvss.karta.framework.runtime.Constants;
import org.mvss.karta.framework.utils.ParserUtils;

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
public class FeatureCompleteEvent extends FeatureEvent
{
   private static final long serialVersionUID = 1L;

   public FeatureCompleteEvent( Event event )
   {
      super( event );
      parameters.put( Constants.RESULT, ParserUtils.convertValue( DataFormat.JSON, parameters.get( Constants.RESULT ), FeatureResult.class ) );
   }

   public FeatureCompleteEvent( String runName, TestFeature feature, FeatureResult result )
   {
      super( StandardEventsTypes.FEATURE_COMPLETE_EVENT, runName, feature );
      this.parameters.put( Constants.RESULT, result );
   }

   @JsonIgnore
   public FeatureResult getResult()
   {
      return (FeatureResult) parameters.get( Constants.RESULT );
   }
}
