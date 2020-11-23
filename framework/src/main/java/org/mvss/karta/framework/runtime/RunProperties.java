package org.mvss.karta.framework.runtime;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunProperties implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID   = 1L;

   private String            name;
   private String            testBed;

   @Builder.Default
   private long              numberOfIterations = 1;
}
