package org.mvss.karta.framework.models.result;

import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores results of a TestFeature execution.
 *
 * @author Manian
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private Date startTime = new Date();

    private Date endTime;

    @Builder.Default
    private volatile boolean successful = true;

    @Builder.Default
    private volatile boolean error = false;

    @Builder.Default
    private ConcurrentHashMap<String, FeatureResult> testResultMap = new ConcurrentHashMap<>();

    public static RunResult error() {
        return RunResult.builder().startTime(new Date()).error(true).successful(false).endTime(new Date()).build();
    }

    public synchronized void addTestResult(FeatureResult result) {
        this.successful = this.successful && result.isPassed();
        testResultMap.put(result.getFeatureName(), result);
    }
}
