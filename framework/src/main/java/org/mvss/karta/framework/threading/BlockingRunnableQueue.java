package org.mvss.karta.framework.threading;

import java.util.concurrent.LinkedBlockingQueue;

public class BlockingRunnableQueue extends LinkedBlockingQueue<Runnable>
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public BlockingRunnableQueue( int capacity )
   {
      super( capacity );
   }

   @Override
   public boolean offer( Runnable runnable )
   {
      while ( true )
      {
         try
         {
            put( runnable );
            break;
         }
         catch ( InterruptedException interruptedException )
         {
            continue;
         }
      }
      return true;
   }
}
