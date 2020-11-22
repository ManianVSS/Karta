package org.mvss.karta.framework.core;

import java.io.Serializable;

import org.mvss.karta.framework.runtime.TestExecutionContext;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder( toBuilder = true )
@NoArgsConstructor
@AllArgsConstructor
public class PreparedStep implements Serializable
{
   /**
    * 
    */
   private static final long    serialVersionUID = 1L;

   private String               identifier;

   private TestExecutionContext testExecutionContext;

   private String               node;
}
