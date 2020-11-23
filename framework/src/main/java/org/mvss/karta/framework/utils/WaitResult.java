package org.mvss.karta.framework.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WaitResult
{
   private boolean   successful;
   private long      startTime;
   private long      endTime;
   private Throwable thrown;

   public long getWaitTime()
   {
      return endTime - startTime;
   }
}
