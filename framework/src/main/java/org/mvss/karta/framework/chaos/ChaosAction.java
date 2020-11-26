package org.mvss.karta.framework.chaos;

import java.io.Serializable;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.framework.randomization.ObjectWithChance;
import org.mvss.karta.framework.utils.DataUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChaosAction implements Serializable, ObjectWithChance
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            name;

   private String            node;

   @Builder.Default
   private float             probability      = 1.0f;

   private ArrayList<String> subjects;

   @Builder.Default
   private float             chaosLevel       = 100.0f;

   @Builder.Default
   private ChaosUnit         chaosUnit        = ChaosUnit.PERCENTAGE;

   public boolean checkForValidity()
   {
      if ( StringUtils.isBlank( name ) )
      {
         return false;
      }

      if ( ( subjects == null ) || ( subjects.isEmpty() ) )
      {
         return false;
      }
      else
      {
         for ( String subject : subjects )
         {
            if ( StringUtils.isBlank( subject ) )
            {
               return false;
            }
         }
      }

      if ( !DataUtils.inRange( chaosLevel, 0.0, 100.0 ) || ( chaosLevel == 0.0 ) )
      {
         return false;
      }

      return true;
   }
}
