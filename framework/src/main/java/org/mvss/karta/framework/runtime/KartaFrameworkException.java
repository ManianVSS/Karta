package org.mvss.karta.framework.runtime;

/**
 * Used to denote unexpected exceptions occurring in Karta Runtime.</br>
 *
 * @author Manian
 */
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
