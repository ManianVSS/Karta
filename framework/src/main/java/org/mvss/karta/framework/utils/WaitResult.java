package org.mvss.karta.framework.utils;

import lombok.*;

import java.time.Duration;
import java.time.Instant;

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

   public static WaitResult failed( Throwable t )
   {
      return new WaitResult( false, Instant.now(), Instant.now(), t );
   }

   public long getWaitTime()
   {
      return endTime.toEpochMilli() - startTime.toEpochMilli();
   }

   public Duration getWaitDuration()
   {
      return Duration.between( startTime, endTime );
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
      this.thrown     = waitResult.thrown;
      return this.successful;
   }
}
