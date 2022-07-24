package org.mvss.karta.framework.nodes.dto;

import lombok.*;
import org.mvss.karta.framework.core.PreparedChaosAction;
import org.mvss.karta.framework.runtime.RunInfo;

import java.io.Serializable;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder( toBuilder = true )
public class ChaosActionRunInfo implements Serializable
{
   private static final long serialVersionUID = 1L;

   private RunInfo             runInfo;
   private PreparedChaosAction preparedChaosAction;
}
