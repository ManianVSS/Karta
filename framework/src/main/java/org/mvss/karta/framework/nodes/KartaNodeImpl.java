package org.mvss.karta.framework.nodes;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.dependencyinjection.TestProperties;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.models.result.FeatureResult;
import org.mvss.karta.framework.models.result.ScenarioResult;
import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.models.result.TestJobResult;
import org.mvss.karta.framework.models.run.RunInfo;
import org.mvss.karta.framework.models.test.*;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.TestFailureException;
import org.mvss.karta.framework.runtime.TestJobRunner;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * The default RMI based Karta node implementation
 *
 * @author Manian
 */
@Log4j2
public class KartaNodeImpl extends UnicastRemoteObject implements KartaNode, Serializable {
    private static final long serialVersionUID = 1L;

    private final transient KartaRuntime kartaRuntime;

    public KartaNodeImpl(KartaRuntime kartaRuntime) throws RemoteException {
        this.kartaRuntime = kartaRuntime;
    }

    @Override
    public void close() throws Exception {
        if (this.kartaRuntime != null) {
            this.kartaRuntime.close();
        }
    }

    @Override
    public FeatureResult runFeature(RunInfo runInfo, TestProperties testProperties, TestFeature feature) throws RemoteException, InterruptedException {
        return kartaRuntime.runFeature(runInfo, testProperties, feature);
    }

    @Override
    public TestJobResult runJobIteration(RunInfo runInfo, String featureName, TestProperties testProperties, TestJob job, int iterationIndex) throws RemoteException {
        try {
            return TestJobRunner.run(kartaRuntime, runInfo, featureName, testProperties, job, iterationIndex, null);
        } catch (Throwable e) {
            throw new RemoteException("Exception while running job iteration", e);
        }
    }

    @Override
    public ScenarioResult runTestScenario(RunInfo runInfo, String featureName, int iterationIndex, PreparedScenario testScenario, long scenarioIterationNumber) throws RemoteException, InterruptedException {
        return kartaRuntime.runTestScenario(runInfo, featureName, iterationIndex, testScenario, scenarioIterationNumber);
    }

    @Override
    public StepResult runStep(RunInfo runInfo, PreparedStep step) throws RemoteException {
        try {
            return kartaRuntime.runStep(runInfo, step);
        } catch (TestFailureException e) {
            return StandardStepResults.failure(e);
        } catch (Throwable t) {
            return StandardStepResults.error(t);
        }
    }

    @Override
    public StepResult performChaosAction(RunInfo runInfo, PreparedChaosAction chaosAction) throws RemoteException {
        try {
            return kartaRuntime.runChaosAction(runInfo, chaosAction);
        } catch (TestFailureException e) {
            return StandardStepResults.failure(e);
        } catch (Throwable t) {
            return StandardStepResults.error(t);
        }
    }

    @Override
    public boolean healthCheck() throws RemoteException {
        log.debug("Health check ping");
        return true;
    }
}
