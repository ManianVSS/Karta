package org.mvss.karta.framework.core;

import java.io.Serializable;

import org.mvss.karta.framework.chaos.ChaosAction;
import org.mvss.karta.framework.runtime.TestExecutionContext;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder( toBuilder = true )
@NoArgsConstructor
@AllArgsConstructor
public class PreparedChaosAction implements Serializable
{
   /**
    * 
    */
   private static final long    serialVersionUID = 1L;

   private ChaosAction          chaosAction;

   private TestExecutionContext testExecutionContext;
}
