package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestJobResult implements Serializable, Comparable<TestJobResult>
{
   /**
    * 
    */
   private static final long          serialVersionUID = 1L;

   @Builder.Default
   private int                        iterationIndex   = 0;

   @Builder.Default
   private Date                       startTime        = new Date();

   private Date                       endTime;

   @Builder.Default
   private boolean                    successsful      = true;

   @Builder.Default
   private boolean                    error            = false;

   @Builder.Default
   private HashMap<TestStep, Boolean> stepResults      = new HashMap<TestStep, Boolean>();

   @Override
   public int compareTo( TestJobResult other )
   {
      return iterationIndex - other.iterationIndex;
   }
}
