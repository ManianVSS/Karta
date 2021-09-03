package org.mvss.karta.framework.runtime;

public class TestFailureException extends Exception
{
   private static final long serialVersionUID = 1L;

   public TestFailureException( String message )
   {
      super( message );
   }

   public TestFailureException( String message, Throwable cause )
   {
      super( message, cause );
   }

   public TestFailureException( Throwable cause )
   {
      super( cause );
   }
}
