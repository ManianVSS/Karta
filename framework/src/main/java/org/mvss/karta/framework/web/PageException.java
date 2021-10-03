package org.mvss.karta.framework.web;

public class PageException extends Exception
{
   private static final long serialVersionUID = 1L;

   public PageException()
   {

   }

   public PageException( String message )
   {
      super( message );
   }

   public PageException( Throwable cause )
   {
      super( cause );
   }

   public PageException( String message, Throwable cause )
   {
      super( message, cause );
   }

   public PageException( String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace )
   {
      super( message, cause, enableSuppression, writableStackTrace );
   }
}
