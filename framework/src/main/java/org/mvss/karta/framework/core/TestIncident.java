package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.HashSet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestIncident implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private HashSet<String>   tags;
   private String            message;
   private Throwable         thrownCause;

   public TestIncident( String message, Throwable thrownCause, String... tags )
   {
      this.message = message;
      this.thrownCause = thrownCause;

      if ( tags != null )
      {
         this.tags = new HashSet<String>();
         for ( String tag : tags )
         {
            this.tags.add( tag );
         }
      }
   }
}
