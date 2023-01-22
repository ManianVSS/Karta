package org.mvss.karta.framework.utils;

import java.util.ArrayList;
import java.util.List;

public class ParallelCausesException extends Exception
{
   private static final long serialVersionUID = 1L;

   public final List<Throwable> causeList = new ArrayList<>();

   public ParallelCausesException( List<Throwable> throwableList )
   {
      this.causeList.addAll( throwableList );
   }

   public ParallelCausesException( String message, List<Throwable> throwableList )
   {
      super( message );
      this.causeList.addAll( throwableList );
   }

   public static ParallelCausesException create( List<Throwable> throwableList )
   {
      StringBuilder combinedMessage = new StringBuilder();
      for ( Throwable throwable : throwableList )
      {
         combinedMessage.append( throwable ).append( "\n" );
      }
      return new ParallelCausesException( combinedMessage.toString(), throwableList );
   }
}
