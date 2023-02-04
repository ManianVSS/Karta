package org.mvss.karta.server.api;

import org.mvss.karta.Constants;
import org.mvss.karta.framework.models.result.FeatureResult;
import org.mvss.karta.framework.models.result.RunResult;
import org.mvss.karta.framework.models.run.RunInfo;
import org.mvss.karta.framework.models.run.RunTarget;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ExecutionController {
    @Autowired
    private KartaRuntime kartaRuntime;

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST, value = Constants.PATH_RUN_TARGET)
    public RunResult startFeatureFileRun(@RequestParam(defaultValue = Constants.UNNAMED) String runName, @RequestBody RunTarget runTarget) {
        if (runName.equals(Constants.UNNAMED)) {
            runName = runName + Constants.HYPHEN + System.currentTimeMillis();
        }

        RunInfo runInfo = kartaRuntime.getDefaultRunInfo().toBuilder().runName(runName).build();
        return kartaRuntime.runTestTarget(runInfo, runTarget);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.POST, value = Constants.PATH_RUN_FEATURE_SOURCE)
    public FeatureResult startFeatureSourceRun(@RequestParam(defaultValue = Constants.UNNAMED) String runName,
                                               @RequestBody String featureSourceString) {
        if (runName.equals(Constants.UNNAMED)) {
            runName = runName + Constants.HYPHEN + System.currentTimeMillis();
        }

        RunInfo runInfo = kartaRuntime.getDefaultRunInfo().toBuilder().runName(runName).build();
        return kartaRuntime.runFeatureSource(runInfo, featureSourceString);
    }
}
