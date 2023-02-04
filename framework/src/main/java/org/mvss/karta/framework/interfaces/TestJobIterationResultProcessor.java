package org.mvss.karta.framework.interfaces;

import org.mvss.karta.framework.models.result.TestJobResult;

/**
 * This functional interface allows processing of result of a test job's iteration typically used for report accumulation
 *
 * @author Manian
 */
@FunctionalInterface
public interface TestJobIterationResultProcessor {
    void consume(String jobName, TestJobResult testJobResult);
}
