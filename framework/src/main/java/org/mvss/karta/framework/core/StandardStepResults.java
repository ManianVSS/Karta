package org.mvss.karta.framework.core;

import java.util.Date;

public class StandardStepResults
{
   public final static StepResult passed = StepResult.builder().successsful( true ).build();
   public final static StepResult failed = StepResult.builder().successsful( false ).build();

   public static StepResult error( Throwable t )
   {
      return error( TestIncident.builder().thrownCause( t ).build() );
   }

   public static StepResult error( TestIncident incident )
   {
      StepResult result = StepResult.builder().error( true ).build();
      result.setEndTime( new Date() );
      result.getIncidents().add( incident );
      return result;
   }

   public static StepResult failure( Throwable t )
   {
      return failure( TestIncident.builder().thrownCause( t ).build() );
   }

   public static StepResult failure( TestIncident incident )
   {
      StepResult result = StepResult.builder().successsful( false ).build();
      result.setEndTime( new Date() );
      result.getIncidents().add( incident );
      return result;
   }
}
