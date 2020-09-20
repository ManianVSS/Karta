package org.mvss.karta.framework.runtime;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunProperties implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            name;
   private String            testBed;
   private long              numberOfIterations;
}
