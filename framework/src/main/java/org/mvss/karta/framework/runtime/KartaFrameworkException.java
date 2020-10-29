package org.mvss.karta.framework.runtime;

public class KartaFrameworkException extends Exception
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   public KartaFrameworkException( String message )
   {
      super( message );
   }

   public KartaFrameworkException( String message, Throwable cause )
   {
      super( message, cause );
   }

   public KartaFrameworkException( Throwable cause )
   {
      super( cause );
   }
}
