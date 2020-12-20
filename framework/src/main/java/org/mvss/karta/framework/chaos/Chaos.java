package org.mvss.karta.framework.chaos;

import java.io.Serializable;

import org.mvss.karta.framework.utils.DataUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Defines an amount of chaos.</br>
 * The amount is subjective to the actual chaos action and implementation.</br>
 * 
 * @see org.mvss.karta.framework.chaos.ChaosAction
 * @author Manian
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder( toBuilder = true )
public class Chaos implements Serializable
{
   private static final long serialVersionUID = 1L;

   /**
    * The level of chaos to perform on the subjects.</br>
    * The unit of chaos is defined by {@link Chaos#chaosUnit}.
    */
   @Builder.Default
   private float             chaosLevel       = 100.0f;

   /**
    * The unit of chaos for {@link Chaos#chaosLevel}.
    */
   @Builder.Default
   private ChaosUnit         chaosUnit        = ChaosUnit.PERCENTAGE;

   /**
    * Validates the chaos amount
    * 
    * @return boolean
    */
   public boolean checkForValidity()
   {
      return DataUtils.inRange( chaosLevel, 0.0, 100.0 ) && ( chaosLevel != 0.0 );
   }

   /**
    * The default chaos amount
    */
   public static Chaos DEFAULT_CHAOS = new Chaos( 100.0f, ChaosUnit.PERCENTAGE );
}
