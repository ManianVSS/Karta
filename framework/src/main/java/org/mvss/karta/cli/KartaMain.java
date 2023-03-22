package org.mvss.karta.cli;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.models.result.FeatureResult;
import org.mvss.karta.framework.models.result.RunResult;
import org.mvss.karta.framework.models.run.RunInfo;
import org.mvss.karta.framework.models.run.RunTarget;
import org.mvss.karta.framework.nodes.KartaNodeServer;
import org.mvss.karta.framework.runtime.KartaRuntime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The entry point for Karta command line interface.</br>
 *
 * @author Manian
 */
@Log4j2
public class KartaMain {
    public static List<Runnable> exitHooks = Collections.synchronizedList(new ArrayList<>());

    private static void jvmExitHook() {
        log.info("******************** Stopping Karta  *********************");
        log.info("Triggering registered exit hooks");
        for (Runnable exitHook : KartaMain.exitHooks) {
            new Thread(exitHook).start();
        }
    }

    public static boolean run(String[] args) {
        Options options = new Options();
        HelpFormatter formatter = new HelpFormatter();
        DefaultParser parser = new DefaultParser();

        options.addOption("t", Constants.TAGS, true, "tags to run");

        options.addOption("f", Constants.FEATURE_FILE, true, "feature file to run");

        options.addOption("j", Constants.JAVA_TEST, true, "test case class to run");
        options.addOption(Constants.JAVA_TEST_JAR, true, "jar file which contains the test");

        options.addOption("r", Constants.RUN_NAME, true, "the name of this test run");

        options.addOption(Constants.RELEASE, true, "the release of the application under test");
        options.addOption(Constants.BUILD, true, "the build of the application under test");

        options.addOption(Constants.NUMBER_OF_ITERATIONS, true, "number of iterations. Applicable only  for feature file/java test");
        options.addOption(Constants.ITERATION_THREAD_COUNT, true,
                "number of threads to run iterations in parallel with. Applicable only for feature file/java test");

        options.addOption(Constants.START_NODE, false, "starts Karta RMI node server");

        options.addOption(null, Constants.HELP, false, "prints this help message");

        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption(Constants.HELP)) {
                formatter.printHelp(Constants.KARTA, options);
                return true;
            } else if (cmd.hasOption(Constants.START_NODE)) {
                KartaRuntime.initializeNodes = false;
                try (KartaRuntime kartaRuntime = KartaRuntime.getInstance()) {
                    if (kartaRuntime == null) {
                        log.error("Karta runtime could not be initialized. Please check the directory and config files");
                        System.exit(-1);
                    }
                    try (KartaNodeServer kartaRMIServer = new KartaNodeServer(kartaRuntime)) {
                        kartaRMIServer.startServer();
                        if (!kartaRuntime.addNodes()) {
                            log.error("Failure in adding nodes");
                        }
                        log.info("Karta node server started " + kartaRMIServer.getNodeConfig());
                        Thread.currentThread().join();
                    }
                }
            } else {
                boolean optionMissing = true;
                boolean runTargetAvailable;

                RunInfo runInfo = new RunInfo();
                RunTarget runTarget = new RunTarget();

                if (cmd.hasOption(Constants.JAVA_TEST)) {
                    optionMissing = false;
                    runTarget.setJavaTest(cmd.getOptionValue(Constants.JAVA_TEST));
                }

                if (cmd.hasOption(Constants.JAVA_TEST_JAR)) {
                    optionMissing = false;
                    runTarget.setJavaTestJarFile(cmd.getOptionValue(Constants.JAVA_TEST_JAR));
                }

                if (cmd.hasOption(Constants.FEATURE_FILE)) {
                    optionMissing = false;
                    runTarget.setFeatureFile(cmd.getOptionValue(Constants.FEATURE_FILE));
                }

                if (cmd.hasOption(Constants.TAGS)) {
                    optionMissing = false;
                    ArrayList<String> tags = new ArrayList<>();
                    Collections.addAll(tags, cmd.getOptionValue(Constants.TAGS).split(Constants.COMMA));
                    runTarget.setRunTags(tags);
                }

                if (cmd.hasOption(Constants.RUN_NAME)) {
                    runInfo.setRunName(cmd.getOptionValue(Constants.RUN_NAME));
                } else {
                    runInfo.setRunName(Constants.UNNAMED + Constants.HYPHEN + System.currentTimeMillis());
                }

                if (cmd.hasOption(Constants.RELEASE)) {
                    runInfo.setRelease(cmd.getOptionValue(Constants.RELEASE));
                }

                if (cmd.hasOption(Constants.BUILD)) {
                    runInfo.setBuild(cmd.getOptionValue(Constants.BUILD));
                }

                if (cmd.hasOption(Constants.NUMBER_OF_ITERATIONS)) {
                    String numberOfIterationsStr = cmd.getOptionValue(Constants.NUMBER_OF_ITERATIONS);

                    if (!StringUtils.isBlank(numberOfIterationsStr)) {
                        runInfo.setNumberOfIterations(Long.parseLong(numberOfIterationsStr));
                    }
                }

                if (cmd.hasOption(Constants.ITERATION_THREAD_COUNT)) {
                    String iterationThreadCountStr = cmd.getOptionValue(Constants.ITERATION_THREAD_COUNT);

                    if (!StringUtils.isBlank(iterationThreadCountStr)) {
                        runInfo.setNumberOfIterationsInParallel(Integer.parseInt(iterationThreadCountStr));
                    }
                }

                Runtime.getRuntime().addShutdownHook(new Thread(KartaMain::jvmExitHook));

                runTargetAvailable = StringUtils.isNotBlank(runTarget.getFeatureFile());
                runTargetAvailable = runTargetAvailable || StringUtils.isNotBlank(runTarget.getJavaTest());
                runTargetAvailable = runTargetAvailable || (runTarget.getRunTags() != null && !runTarget.getRunTags().isEmpty());

                if (runTargetAvailable) {
                    try (KartaRuntime kartaRuntime = KartaRuntime.getInstance()) {
                        if (kartaRuntime == null) {
                            log.error("Karta runtime could not be initialized. Please check the directory and config files");
                            System.exit(-1);
                        }
                        RunResult runResult = kartaRuntime.runTestTarget(runInfo, runTarget);
                        System.out.println("Run results are as follows: ");
                        ConcurrentHashMap<String, FeatureResult> resultMap = runResult.getTestResultMap();
                        for (Entry<String, FeatureResult> entry : resultMap.entrySet()) {
                            System.out.println(entry.getKey() + Constants.COLON + Constants.SPACE + (entry.getValue().isPassed() ?
                                    Constants.PASS :
                                    Constants.FAIL));
                        }

                        return runResult.isSuccessful();
                    }
                } else {
                    if (optionMissing) {
                        formatter.printHelp(Constants.KARTA, options);
                    } else {
                        log.error("Run target not available");
                    }
                    return false;
                }
            }
        } catch (UnrecognizedOptionException | MissingArgumentException uoe) {
            System.err.println(uoe.getMessage());
            formatter.printHelp(Constants.KARTA, options);
            return false;
        } catch (InterruptedException ie) {
            log.error("Run interrupted");
            return false;
        } catch (Throwable t) {
            log.error("Exception caught while init", t);
            formatter.printHelp(Constants.KARTA, options);
            return false;
        }
        return false;
    }

    public static void main(String[] args) {
        if (!run(args)) {
            System.exit(-1);
        }
    }
}
