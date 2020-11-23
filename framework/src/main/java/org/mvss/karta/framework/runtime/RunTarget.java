package org.mvss.karta.framework.runtime;

import java.io.Serializable;
import java.util.HashSet;

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
public class RunTarget implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   private String            featureFile;

   private String            javaTest;
   private String            javaTestJarFile;

   private HashSet<String>   runTags;
}
