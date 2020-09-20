package org.mvss.karta.framework.randomization;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProbableData implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private Range             range;

   private Object[]          values;

   private int               probability;

   public ProbableData( int probability, Range range )
   {
      this.range = range;
      this.probability = probability;
   }

   public ProbableData( int probability, Object... values )
   {
      this.values = values;
      this.probability = probability;
   }
}
