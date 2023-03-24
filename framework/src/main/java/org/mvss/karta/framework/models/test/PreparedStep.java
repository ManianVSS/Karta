package org.mvss.karta.framework.models.test;

import lombok.*;
import org.mvss.karta.framework.models.run.TestExecutionContext;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Prepared step for execution
 *
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PreparedStep implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gwtConjunction;

    /**
     * The step identifier.
     */
    private String identifier;

    /**
     * The test execution context object for running the step.
     */
    private TestExecutionContext testExecutionContext;

    /**
     * The remote node on which to run the step on.
     */
    private String node;

    /**
     * Indicates if the same step is to be run in multiple threads in parallel.
     */
    @Builder.Default
    private Integer numberOfThreads = 1;

    /**
     * The number of times this step should be retried if failed
     */
    @Builder.Default
    private Integer maxRetries = 0;

    /**
     * The group of prepared steps to run.
     */
    private ArrayList<PreparedStep> steps;

    /**
     * Indicates if this is a group of steps to be run in parallel or in sequence.
     */
    @Builder.Default
    private Boolean runStepsInParallel = false;

    /**
     * The condition identifier indicates if the step is to be run
     */
    @Builder.Default
    private String condition = null;

}
