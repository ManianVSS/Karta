package org.mvss.karta.server;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BadRequestException extends Exception
{
   private static final long serialVersionUID = 1L;

   public BadRequestException( String message )
   {
      super( message );
   }

   public BadRequestException( Throwable t )
   {
      super( t );
   }

   public BadRequestException( String message, Throwable t )
   {
      super( message, t );
   }
}
