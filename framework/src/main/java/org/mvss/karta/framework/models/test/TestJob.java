package org.mvss.karta.framework.models.test;

import lombok.*;
import org.mvss.karta.framework.enums.JobType;
import org.mvss.karta.framework.models.chaos.ChaosActionTreeNode;
import org.quartz.SimpleTrigger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class describes a test job object.
 *
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class TestJob implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;

    @Builder.Default
    private boolean repeat = false;

    @Builder.Default
    private long interval = -1L;

    @Builder.Default
    private JobType jobType = JobType.STEPS;

    @Builder.Default
    private ArrayList<TestStep> steps = new ArrayList<>();

    private ChaosActionTreeNode chaosConfiguration;

    @Builder.Default
    private int iterationCount = SimpleTrigger.REPEAT_INDEFINITELY;

    private String node;

    private HashMap<String, ArrayList<Serializable>> testDataSet;

    private boolean daemonProcess;
}
