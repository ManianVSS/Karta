package org.mvss.karta.framework.runtime;

import lombok.*;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.TestProperties;
import org.mvss.karta.framework.models.result.FeatureResult;
import org.mvss.karta.framework.models.run.RunInfo;
import org.mvss.karta.framework.models.test.TestFeature;
import org.mvss.karta.framework.nodes.KartaNode;

import java.util.concurrent.Callable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Log4j2
@Builder
public class RemoteFeatureRunner implements Callable<FeatureResult> {
    private RunInfo runInfo;
    private TestProperties testProperties;
    private TestFeature testFeature;

    @Builder.Default
    private KartaNode minionToUse = null;

    private FeatureResult result;

    @Override
    public FeatureResult call() {
        try {
            result = minionToUse.runFeature(runInfo, testProperties, testFeature);
            result.processRemoteResults();
        } catch (Throwable t) {
            log.error(Constants.EMPTY_STRING, t);
            result.setSuccessful(false);
        }
        return result;
    }
}
