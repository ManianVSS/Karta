package org.mvss.karta.framework.core;

import java.util.Date;

public class StandardScenarioResults
{
   public final static ScenarioResult passed = ScenarioResult.builder().successsful( true ).build();
   public final static ScenarioResult failed = ScenarioResult.builder().successsful( false ).build();

   public static ScenarioResult error( Throwable t )
   {
      return error( TestIncident.builder().thrownCause( t ).build() );
   }

   public static ScenarioResult error( TestIncident incident )
   {
      ScenarioResult result = ScenarioResult.builder().error( true ).build();
      result.setEndTime( new Date() );
      result.getIncidents().add( incident );
      return result;
   }

   public static ScenarioResult failure( Throwable t )
   {
      return failure( TestIncident.builder().thrownCause( t ).build() );
   }

   public static ScenarioResult failure( TestIncident incident )
   {
      ScenarioResult result = ScenarioResult.builder().successsful( false ).build();
      result.setEndTime( new Date() );
      result.getIncidents().add( incident );
      return result;
   }
}