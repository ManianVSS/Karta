package org.mvss.karta.framework.utils;

import java.time.Duration;
import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This class groups the results for a wait activity
 * 
 * @author Manian
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaitResult
{
   @Builder.Default
   private boolean   successful = true;
   private Instant   startTime;
   private Instant   endTime;
   private Throwable thrown;

   public long getWaitTime()
   {
      return endTime.toEpochMilli() - startTime.toEpochMilli();
   }

   public Duration getWaitDuration()
   {
      return Duration.between( startTime, endTime );
   }

   public static WaitResult failed( Throwable t )
   {
      return new WaitResult( false, Instant.now(), Instant.now(), t );
   }

   public boolean accumulate( WaitResult waitResult )
   {
      if ( this.startTime == null )
      {
         this.startTime = waitResult.startTime;
      }
      if ( waitResult.endTime != null )
      {
         this.endTime = waitResult.endTime;
      }
      this.successful = this.successful && waitResult.successful;
      this.thrown = waitResult.thrown;
      return this.successful;
   }
}
