package org.mvss.karta.framework.utils;

public class WaitUtil
{
   public static void sleep( long milliseconds )
   {
      long currentTime = System.currentTimeMillis();
      long sleepUntil = currentTime + milliseconds;

      do
      {
         try
         {
            Thread.sleep( sleepUntil - currentTime );
         }
         catch ( InterruptedException ie )
         {

         }
      }
      while ( sleepUntil > ( currentTime = System.currentTimeMillis() ) );
   }
}
