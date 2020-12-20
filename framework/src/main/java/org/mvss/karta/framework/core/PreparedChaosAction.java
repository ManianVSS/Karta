package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;

import org.mvss.karta.framework.chaos.Chaos;
import org.mvss.karta.framework.runtime.TestExecutionContext;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Describes a prepared chaos action ready for execution.</br>
 * Prepared chaos actions are prepared from ChaosActions selected by chaos engine.</br>
 * 
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder( toBuilder = true )
@NoArgsConstructor
@AllArgsConstructor
public class PreparedChaosAction implements Serializable
{

   private static final long    serialVersionUID     = 1L;

   /**
    * The name of the chaos action.</br>
    * This is used to map the chaos action definition by Kriya plug-in using {@link org.mvss.karta.framework.core.ChaosActionDefinition#value()}
    */
   @Builder.Default
   private String               name                 = null;

   /**
    * The name of the node on which to run prepared chaos action.</br>
    * If running chaos action locally to be set to null.</br>
    */
   @Builder.Default
   private String               node                 = null;

   /**
    * The list of subject names on which to apply the chaos action.</br>
    * The subject names are subjective to the implementation of the chaos action and not relevant to the chaos engine. </br>
    */
   @Builder.Default
   private ArrayList<String>    subjects             = null;

   /**
    * The chaos amount for this chaos action.</br>
    * Refer to {@link org.mvss.karta.framework.chaos.Chaos}
    */
   @Builder.Default
   private Chaos                chaos                = null;

   /**
    * The test execution context to use for execution.
    */
   @Builder.Default
   private TestExecutionContext testExecutionContext = null;
}
