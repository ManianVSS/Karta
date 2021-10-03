package org.mvss.karta.framework.threading;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * An implementation of LinkedBlockingQueue&lt;Runnable&gt; which blocks while queue is full and does not fail on interruption
 *
 * @author Manian
 */
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
         catch ( InterruptedException ignored )
         {
         }
      }
      return true;
   }
}
