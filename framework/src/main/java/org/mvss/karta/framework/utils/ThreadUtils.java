package org.mvss.karta.framework.utils;

import java.util.List;

public class ThreadUtils
{
   public static void waitForAllThreads( List<Thread> threadsToWaitFor )
   {
      for ( Thread threadToWait : threadsToWaitFor )
      {
         try
         {
            threadToWait.join();
         }
         catch ( InterruptedException ie )
         {

         }
      }
   }
}
