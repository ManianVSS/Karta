package org.mvss.karta.framework.core;

import java.io.Serializable;
import java.util.ArrayList;

import org.mvss.karta.framework.chaos.ChaosActionTreeNode;
import org.mvss.karta.framework.enums.JobType;
import org.quartz.SimpleTrigger;

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
@AllArgsConstructor
@NoArgsConstructor
public class TestJob implements Serializable
{
   /**
    * 
    */
   private static final long   serialVersionUID = 1L;

   private String              name;

   @Builder.Default
   private boolean             repeat           = false;

   @Builder.Default
   private long                interval         = -1l;

   @Builder.Default
   private JobType             jobType          = JobType.STEPS;

   @Builder.Default
   private ArrayList<TestStep> steps            = new ArrayList<TestStep>();

   private ChaosActionTreeNode chaosConfiguration;

   @Builder.Default
   private int                 iterationCount   = SimpleTrigger.REPEAT_INDEFINITELY;

   private String              node;
}
