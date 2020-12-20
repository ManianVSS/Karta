package org.mvss.karta.framework.core;

import java.io.Serializable;

import org.mvss.karta.framework.runtime.TestExecutionContext;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Prepared step for execution
 * 
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder( toBuilder = true )
@NoArgsConstructor
@AllArgsConstructor
public class PreparedStep implements Serializable
{
   private static final long    serialVersionUID = 1L;

   private String               identifier;

   private TestExecutionContext testExecutionContext;

   private String               node;
}
