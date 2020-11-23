package org.mvss.karta.framework.randomization;

import java.io.Serializable;
import java.util.Random;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Range implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private long              min              = 0;
   private long              max              = Long.MAX_VALUE;

   public long getNext( Random random )
   {
      if ( max < min )
      {
         long temp = max;
         max = min;
         min = temp;
      }
      if ( max == min )
      {
         return min;
      }

      return min + Math.abs( random.nextLong() ) % ( max - min );
   }
}
