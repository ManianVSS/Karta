package org.mvss.karta.framework.runtime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunTarget
{

   private String featureFile;

   private String javaTest;
   private String javaTestJarFile;

}