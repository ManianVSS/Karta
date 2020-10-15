package org.mvss.karta.framework.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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
