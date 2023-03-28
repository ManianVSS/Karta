package org.mvss.karta.framework.plugins.impl;

import groovy.lang.GroovyClassLoader;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.mvss.karta.Constants;
import org.mvss.karta.dependencyinjection.annotations.Initializer;
import org.mvss.karta.dependencyinjection.annotations.KartaAutoWired;
import org.mvss.karta.dependencyinjection.annotations.PropertyMapping;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.models.run.TestExecutionContext;
import org.mvss.karta.framework.models.test.PreparedChaosAction;
import org.mvss.karta.framework.models.test.PreparedStep;
import org.mvss.karta.framework.plugins.StepRunner;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.TestFailureException;
import org.python.core.PySyntaxError;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class GroovyStepRunner implements StepRunner {
    public static final String PLUGIN_NAME = "GroovyStepRunner";

    public static final String INLINE_STEP_DEF_PARAM_INDICATOR_STRING = "\"\"";
    public static final String WORD_FETCH_REGEX = "\\W+";

    public static final String INLINE_TEST_DATA_PATTERN = "\"(?:[^\\\\\"]+|\\\\.|\\\\\\\\)*\"";
    public static final List<String> conjunctions = Arrays.asList("Given", "When", "Then", "And", "But");
    private final static Pattern testDataPattern = Pattern.compile(INLINE_TEST_DATA_PATTERN);

    @PropertyMapping(group = PLUGIN_NAME, value = "stepHandlerMap")
    private HashMap<String, String> stepHandlerMap = new HashMap<>();
    @PropertyMapping(group = PLUGIN_NAME, value = "chaosActionHandlerMap")
    private HashMap<String, String> chaosActionHandlerMap = new HashMap<>();
    @PropertyMapping(group = PLUGIN_NAME, value = "conditionDefinitionMap")
    private HashMap<String, String> conditionDefinitionMap = new HashMap<>();
    private boolean initialized = false;
    @KartaAutoWired
    private KartaRuntime kartaRuntime;

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }


    @Initializer
    public boolean initialize() throws Throwable {
        if (initialized) {
            return true;
        }
        log.info("Initializing " + PLUGIN_NAME + " plugin");


        initialized = true;
        return true;
    }

    @Override
    public String sanitizeStepIdentifier(String stepIdentifier) {
        // Handle null stepIdentifier
        if (StringUtils.isBlank(stepIdentifier)) {
            return stepIdentifier;
        }

        stepIdentifier = stepIdentifier.trim();
        String[] words = stepIdentifier.split(WORD_FETCH_REGEX);
        String conjunctionUsed;
        if (conjunctions.contains(words[0])) {
            conjunctionUsed = words[0];
            stepIdentifier = stepIdentifier.substring(conjunctionUsed.length()).trim();
        }
        stepIdentifier = stepIdentifier.replaceAll(INLINE_TEST_DATA_PATTERN, INLINE_STEP_DEF_PARAM_INDICATOR_STRING);

        return stepIdentifier;
    }

    @Override
    public boolean stepImplemented(String identifier) {
        return stepHandlerMap.containsKey(sanitizeStepIdentifier(identifier));
    }


    @Override
    public StepResult runStep(PreparedStep testStep) throws TestFailureException {
        StepResult result = new StepResult();
        TestExecutionContext testExecutionContext = testStep.getTestExecutionContext();

        log.debug("Step run" + testStep);

        if (StringUtils.isBlank(testStep.getIdentifier())) {
            log.error("Empty step definition identifier for step " + testStep);
            return result;
        }

        String stepIdentifier = testStep.getIdentifier();

        stepIdentifier = sanitizeStepIdentifier(stepIdentifier);
        if (!stepHandlerMap.containsKey(stepIdentifier)) {
            String errorMessage = "Missing step definition: " + stepIdentifier;
            log.error(errorMessage);
            return StandardStepResults.error(errorMessage);
        }

        // Fetch the positional argument names
        ArrayList<String> inlineStepDefinitionTestData = new ArrayList<>();
        Matcher matcher = testDataPattern.matcher(testStep.getIdentifier().trim());
        while (matcher.find()) {
            inlineStepDefinitionTestData.add(matcher.group());
        }

        try {
            String stepMapping = stepHandlerMap.get(stepIdentifier);
            try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader()) {
                Class<?> scriptClass = groovyClassLoader.parseClass(new File(stepMapping));
                Object scriptInstance = scriptClass.getDeclaredConstructor().newInstance();
                kartaRuntime.initializeObject(scriptInstance);
                testExecutionContext.getContextBeanRegistry().loadBeans(scriptInstance);
                String[] args = new String[inlineStepDefinitionTestData.size()];
                inlineStepDefinitionTestData.toArray(args);
                result = (StepResult) scriptClass.getDeclaredMethod("run", new Class[]{PreparedStep.class, Object[].class}).invoke(scriptInstance, testStep, args);
            } catch (PySyntaxError pySyntaxError) {
                String errorMessage = "Check jython script syntax for file  " + stepMapping;
                log.error(errorMessage, pySyntaxError);
                result = StandardStepResults.error(errorMessage, pySyntaxError);
            }
        } catch (Throwable t) {
            String errorMessage = "Exception occurred while running step definition " + testStep;
            log.error(errorMessage, t);
            result = StandardStepResults.error(errorMessage, t);
        }

        result.setEndTime(new Date());
        return result;
    }

    @Override
    public boolean chaosActionImplemented(String name) {
        return chaosActionHandlerMap.containsKey(name);
    }

    @Override
    public StepResult performChaosAction(PreparedChaosAction preparedChaosAction) {
        StepResult result = new StepResult();

        TestExecutionContext testExecutionContext = preparedChaosAction.getTestExecutionContext();
        log.debug("Chaos actions run" + preparedChaosAction);

        if (StringUtils.isBlank(preparedChaosAction.getName())) {
            log.error("Empty chaos action name " + preparedChaosAction);
            return result;
        }

        String chaosActionName = preparedChaosAction.getName();

        try {
            if (!chaosActionHandlerMap.containsKey(chaosActionName)) {
                // TODO: Handling undefined chaos action to ask manual action(other configured handlers) if possible
                String errorMessage = "Missing chaos action handler definition: " + chaosActionName;
                log.error(errorMessage);
                log.error("Suggestion:\r\n   @ChaosActionDefinition( \"" + StringEscapeUtils.escapeJava(chaosActionName) + "\" )\r\n" + "   public StepResult " + chaosActionName.replaceAll(Constants.REGEX_NON_ALPHANUMERIC, Constants.UNDERSCORE) + "( TestExecutionContext context, PreparedChaosAction actionToPerform) throws Throwable\r\n" + "   {\r\n...\r\n   }");
                return StandardStepResults.error(errorMessage);
            }

            String chaosActionMapping = chaosActionHandlerMap.get(chaosActionName);
            try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader()) {
                Class<?> scriptClass = groovyClassLoader.parseClass(new File(chaosActionMapping));
                Object scriptInstance = scriptClass.getDeclaredConstructor().newInstance();
                kartaRuntime.initializeObject(scriptInstance);
                testExecutionContext.getContextBeanRegistry().loadBeans(scriptInstance);
                result = (StepResult) scriptClass.getDeclaredMethod("run", new Class[]{PreparedChaosAction.class}).invoke(scriptInstance, preparedChaosAction);
            } catch (PySyntaxError pySyntaxError) {
                String errorMessage = "Check jython script syntax for file  " + chaosActionMapping;
                log.error(errorMessage, pySyntaxError);
                result = StandardStepResults.error(errorMessage, pySyntaxError);
            }
        } catch (Throwable t) {
            String errorMessage = "Exception occurred while running chaos action " + preparedChaosAction;
            log.error(errorMessage, t);
            result = StandardStepResults.error(errorMessage, t);
        }

        result.setEndTime(new Date());
        return result;
    }

    @Override
    public boolean conditionImplemented(String conditionIdentifier) {
        return conditionDefinitionMap.containsKey(sanitizeStepIdentifier(conditionIdentifier));
    }

    @Override
    public boolean runCondition(TestExecutionContext testExecutionContext, String conditionIdentifier) {
        log.debug("Condition check" + conditionIdentifier);

        if (StringUtils.isBlank(conditionIdentifier)) {
            log.error("Empty condition definition identifier " + conditionIdentifier);
            return false;
        }

        conditionIdentifier = sanitizeStepIdentifier(conditionIdentifier);
        if (!conditionDefinitionMap.containsKey(conditionIdentifier)) {
            String errorMessage = "Missing condition definition: " + conditionIdentifier;
            log.error(errorMessage);
            return false;
        }

        try {
            String conditionMapping = conditionDefinitionMap.get(conditionIdentifier);
            try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader()) {
                Class<?> scriptClass = groovyClassLoader.parseClass(new File(conditionMapping));
                Object scriptInstance = scriptClass.getDeclaredConstructor().newInstance();
                kartaRuntime.initializeObject(scriptInstance);
                testExecutionContext.getContextBeanRegistry().loadBeans(scriptInstance);
                return (Boolean) scriptClass.getDeclaredMethod("run", new Class[]{TestExecutionContext.class}).invoke(scriptInstance, testExecutionContext);
            } catch (PySyntaxError pySyntaxError) {
                String errorMessage = "Check jython script syntax for file  " + conditionMapping;
                log.error(errorMessage, pySyntaxError);
                return false;
            }
        } catch (Throwable t) {
            String errorMessage = "Exception occurred while running step definition " + conditionIdentifier;
            log.error(errorMessage, t);
            return false;
        }
    }

}
