package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.HashSet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunTarget implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID   = 1L;

   private String            featureFile;

   private String            javaTest;
   private String            javaTestJarFile;

   @Builder.Default
   private long              numberOfIterations = 1;

   @Builder.Default
   private int               numberOfThreads    = 1;

   private HashSet<String>   tags;
}
