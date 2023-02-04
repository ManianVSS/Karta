package org.mvss.karta.framework.plugins.impl.kriya;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.mvss.karta.Constants;
import org.mvss.karta.framework.annotations.*;
import org.mvss.karta.framework.core.StandardStepResults;
import org.mvss.karta.framework.enums.StepOutputType;
import org.mvss.karta.framework.models.generic.Pair;
import org.mvss.karta.framework.models.result.ScenarioResult;
import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.models.run.TestExecutionContext;
import org.mvss.karta.framework.models.test.PreparedChaosAction;
import org.mvss.karta.framework.models.test.PreparedScenario;
import org.mvss.karta.framework.models.test.PreparedStep;
import org.mvss.karta.framework.models.test.TestFeature;
import org.mvss.karta.framework.plugins.FeatureSourceParser;
import org.mvss.karta.framework.plugins.StepRunner;
import org.mvss.karta.framework.plugins.TestLifeCycleHook;
import org.mvss.karta.framework.properties.PropertyMapping;
import org.mvss.karta.framework.runtime.BeanRegistry;
import org.mvss.karta.framework.runtime.KartaRuntime;
import org.mvss.karta.framework.runtime.TestFailureException;
import org.mvss.karta.framework.utils.AnnotationScanner;
import org.mvss.karta.framework.utils.ParserUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class KriyaPlugin implements FeatureSourceParser, StepRunner, TestLifeCycleHook {
    public static final String PLUGIN_NAME = "Kriya";

    public static final String INLINE_STEP_DEF_PARAM_INDICATOR_STRING = "\"\"";
    public static final String WORD_FETCH_REGEX = "\\W+";

    public static final String INLINE_TEST_DATA_PATTERN = "\"(?:[^\\\\\"]+|\\\\.|\\\\\\\\)*\"";
    public static final List<String> conjunctions = Arrays.asList("Given", "When", "Then", "And", "But");
    private final static Pattern testDataPattern = Pattern.compile(INLINE_TEST_DATA_PATTERN);
    private static final ObjectMapper objectMapper = ParserUtils.getObjectMapper();
    private final HashMap<String, Pattern> tagPatternMap = new HashMap<>();
    private final HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedRunStartHooks = new HashMap<>();
    private final HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedRunStopHooks = new HashMap<>();
    private final HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedFeatureStartHooks = new HashMap<>();
    private final HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedFeatureStopHooks = new HashMap<>();
    private final HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedScenarioStartHooks = new HashMap<>();
    private final HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedScenarioStopHooks = new HashMap<>();
    private final HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedScenarioFailureHooks = new HashMap<>();
    private final HashMap<String, Pair<Object, Method>> stepHandlerMap = new HashMap<>();
    private final HashMap<String, Pair<Object, Method>> chaosActionHandlerMap = new HashMap<>();
    private final HashMap<String, Pair<Object, Method>> conditionDefinitionMap = new HashMap<>();
    private final BeanRegistry initializedClassesRegistry = new BeanRegistry();
    private boolean initialized = false;
    @PropertyMapping(group = PLUGIN_NAME, value = "stepDefinitionPackageNames")
    private ArrayList<String> stepDefinitionPackageNames;
    @KartaAutoWired
    private KartaRuntime kartaRuntime;
    private final Consumer<Method> processTaggedRunStartHook = runStartHookMethod -> {
        try {
            for (BeforeRun beforeRun : runStartHookMethod.getAnnotationsByType(BeforeRun.class)) {
                String[] tags = beforeRun.value();
                processTaggedHook(taggedRunStartHooks, tags, runStartHookMethod, String.class);
            }
        } catch (Throwable t) {
            log.error("Exception while parsing run start hook from method  " + runStartHookMethod.getName(), t);
        }
    };
    private final Consumer<Method> processTaggedRunStopHook = runStopHookMethod -> {
        try {
            for (AfterRun afterRun : runStopHookMethod.getAnnotationsByType(AfterRun.class)) {
                String[] tags = afterRun.value();
                processTaggedHook(taggedRunStopHooks, tags, runStopHookMethod, String.class);
            }
        } catch (Throwable t) {
            log.error("Exception while parsing run stop hook from method  " + runStopHookMethod.getName(), t);
        }
    };
    private final Consumer<Method> processTaggedFeatureStartHook = featureStartHookMethod -> {
        try {
            for (BeforeFeature beforeFeature : featureStartHookMethod.getAnnotationsByType(BeforeFeature.class)) {
                String[] tags = beforeFeature.value();
                processTaggedHook(taggedFeatureStartHooks, tags, featureStartHookMethod, String.class, TestFeature.class);
            }
        } catch (Throwable t) {
            log.error("Exception while parsing feature start hook from method  " + featureStartHookMethod.getName(), t);
        }
    };
    private final Consumer<Method> processTaggedFeatureStopHook = featureStopHookMethod -> {
        try {
            for (AfterFeature afterFeature : featureStopHookMethod.getAnnotationsByType(AfterFeature.class)) {
                String[] tags = afterFeature.value();
                processTaggedHook(taggedFeatureStopHooks, tags, featureStopHookMethod, String.class, TestFeature.class);
            }
        } catch (Throwable t) {
            log.error("Exception while parsing feature stop hook from method  " + featureStopHookMethod.getName(), t);
        }
    };
    private final Consumer<Method> processTaggedScenarioStartHook = scenarioStartHookMethod -> {
        try {
            for (BeforeScenario beforeScenario : scenarioStartHookMethod.getAnnotationsByType(BeforeScenario.class)) {
                String[] tags = beforeScenario.value();
                processTaggedHook(taggedScenarioStartHooks, tags, scenarioStartHookMethod, String.class, String.class, PreparedScenario.class);
            }
        } catch (Throwable t) {
            log.error("Exception while parsing scenario start hook from method  " + scenarioStartHookMethod.getName(), t);
        }
    };
    private final Consumer<Method> processTaggedScenarioStopHook = scenarioStopHookMethod -> {
        try {
            for (AfterScenario afterScenario : scenarioStopHookMethod.getAnnotationsByType(AfterScenario.class)) {
                String[] tags = afterScenario.value();
                processTaggedHook(taggedScenarioStopHooks, tags, scenarioStopHookMethod, String.class, String.class, PreparedScenario.class);
            }
        } catch (Throwable t) {
            log.error("Exception while parsing scenario stop hook from method  " + scenarioStopHookMethod.getName(), t);
        }
    };
    private final Consumer<Method> processTaggedScenarioFailedHook = scenarioFailureHookMethod -> {
        try {
            for (ScenarioFailed afterScenario : scenarioFailureHookMethod.getAnnotationsByType(ScenarioFailed.class)) {
                String[] tags = afterScenario.value();
                processTaggedHook(taggedScenarioFailureHooks, tags, scenarioFailureHookMethod, String.class, String.class, PreparedScenario.class, ScenarioResult.class);
            }
        } catch (Throwable t) {
            log.error("Exception while parsing scenario failure hook from method  " + scenarioFailureHookMethod.getName(), t);
        }
    };

    @Override
    public String getPluginName() {
        return PLUGIN_NAME;
    }

    public void processStepDefinitionMethod(Method candidateStepDefinitionMethod) {
        try {
            for (StepDefinition stepDefinition : candidateStepDefinitionMethod.getAnnotationsByType(StepDefinition.class)) {
                String methodDescription = candidateStepDefinitionMethod.toString();
                String stepDefString = stepDefinition.value();

                if (stepHandlerMap.containsKey(stepDefString)) {
                    log.error("Step definition is duplicate " + methodDescription);
                    continue;
                }

                Parameter[] params = candidateStepDefinitionMethod.getParameters();

                int positionalArgumentsCount = 0;
                for (Parameter param : params) {
                    if ((param.getType() != TestExecutionContext.class) && (param.getAnnotation(TestData.class) == null) && (param.getAnnotation(ContextBean.class) == null) && (param.getAnnotation(ContextVariable.class) == null)) {
                        positionalArgumentsCount++;
                    }
                }

                if (positionalArgumentsCount != StringUtils.countMatches(stepDefString, INLINE_STEP_DEF_PARAM_INDICATOR_STRING)) {
                    log.error("Step definition method " + methodDescription + " does not match the argument count as per the identifier");
                    continue;
                }

                log.debug("Mapping step definition " + stepDefString + " to " + methodDescription);

                Class<?> stepDefinitionClass = candidateStepDefinitionMethod.getDeclaringClass();

                Object stepDefClassObj = initializedClassesRegistry.get(stepDefinitionClass.getName());
                if (stepDefClassObj == null) {
                    stepDefClassObj = stepDefinitionClass.getDeclaredConstructor().newInstance();
                    kartaRuntime.initializeObject(stepDefClassObj);
                    initializedClassesRegistry.add(stepDefClassObj);
                }
                stepHandlerMap.put(stepDefString, new Pair<>(stepDefClassObj, candidateStepDefinitionMethod));
            }
        } catch (Throwable t) {
            log.error("Exception while parsing step definition from method  " + candidateStepDefinitionMethod.getName(), t);
        }
    }

    public void processConditionMethod(Method candidateConditionMethod) {
        try {
            for (ConditionDefinition conditionDefinition : candidateConditionMethod.getAnnotationsByType(ConditionDefinition.class)) {
                String methodDescription = candidateConditionMethod.toString();
                String conditionDefString = conditionDefinition.value();

                if (conditionDefinitionMap.containsKey(conditionDefString)) {
                    log.error("Condition definition is duplicate " + methodDescription);
                    continue;
                }

                if ((candidateConditionMethod.getReturnType() != boolean.class) && ((candidateConditionMethod.getReturnType() != Boolean.class))) {
                    log.error("Condition definition method " + methodDescription + " should return boolean");
                    continue;
                }

                Parameter[] params = candidateConditionMethod.getParameters();
                int positionalArgumentsCount = 0;
                for (Parameter param : params) {
                    if ((param.getType() != TestExecutionContext.class) && (param.getAnnotation(TestData.class) == null) && (param.getAnnotation(ContextBean.class) == null) && (param.getAnnotation(ContextVariable.class) == null)) {
                        positionalArgumentsCount++;
                    }
                }

                if (positionalArgumentsCount != StringUtils.countMatches(conditionDefString, INLINE_STEP_DEF_PARAM_INDICATOR_STRING)) {
                    log.error("Candidate condition definition method " + methodDescription + " does not match the argument count as per the identifier");
                    continue;
                }

                log.debug("Mapping condition definition " + conditionDefString + " to " + methodDescription);

                Class<?> conditionDefinitionClass = candidateConditionMethod.getDeclaringClass();

                Object conditionDefClassObj = initializedClassesRegistry.get(conditionDefinitionClass.getName());
                if (conditionDefClassObj == null) {
                    conditionDefClassObj = conditionDefinitionClass.getDeclaredConstructor().newInstance();
                    kartaRuntime.initializeObject(conditionDefClassObj);
                    initializedClassesRegistry.add(conditionDefClassObj);
                }

                conditionDefinitionMap.put(conditionDefString, new Pair<>(conditionDefClassObj, candidateConditionMethod));
            }
        } catch (Throwable t) {
            log.error("Exception while parsing condition definition from method  " + candidateConditionMethod.getName(), t);
        }

    }

    public void processChaosDefinitionMethod(Method candidateChaosActionMethod) {
        try {
            for (ChaosActionDefinition chaosActionDefinition : candidateChaosActionMethod.getAnnotationsByType(ChaosActionDefinition.class)) {
                String methodDescription = candidateChaosActionMethod.toString();
                String chaosActionName = chaosActionDefinition.value();

                if (chaosActionHandlerMap.containsKey(chaosActionName)) {
                    log.error("Chaos action definition is duplicate " + methodDescription);
                    continue;
                }

                Parameter[] params = candidateChaosActionMethod.getParameters();

                for (Parameter param : params) {
                    Class<?> paramType = param.getType();

                    if ((paramType != TestExecutionContext.class) && (paramType != PreparedChaosAction.class)) {
                        TestData testDataAnnotation = param.getAnnotation(TestData.class);
                        ContextBean contextBeanAnnotation = param.getAnnotation(ContextBean.class);
                        ContextVariable contextVariableAnnotation = param.getAnnotation(ContextVariable.class);

                        if ((testDataAnnotation == null) && (contextBeanAnnotation == null) && (contextVariableAnnotation == null)) {
                            log.error("Chaos action definition method " + methodDescription + "'s parameter is not mapped mapped with an appropriate annotation(" + TestData.class.getName() + ", " + ContextBean.class.getName() + ", " + ContextVariable.class.getName() + ")");
                        }
                    }
                }

                log.debug("Mapping chaos action definition " + chaosActionName + " to " + methodDescription);

                Class<?> chaosActionDefinitionClass = candidateChaosActionMethod.getDeclaringClass();

                Object chaosActionDefClassObj = initializedClassesRegistry.get(chaosActionDefinitionClass.getName());
                if (chaosActionDefClassObj == null) {
                    chaosActionDefClassObj = chaosActionDefinitionClass.getDeclaredConstructor().newInstance();
                    kartaRuntime.initializeObject(chaosActionDefClassObj);
                    initializedClassesRegistry.add(chaosActionDefClassObj);
                }

                chaosActionHandlerMap.put(chaosActionName, new Pair<>(chaosActionDefClassObj, candidateChaosActionMethod));
            }
        } catch (Throwable t) {
            log.error("Exception while parsing chaos action definition from method  " + candidateChaosActionMethod.getName(), t);
        }
    }

    private void processTaggedHook(HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedHooks, String[] tags, Method hookMethod, Class<?>... parameters) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        String methodDescription = hookMethod.toString();
        Class<?>[] params = hookMethod.getParameterTypes();

        if (parameters != null) {
            boolean error = (params.length != parameters.length);

            for (int i = 0; !error && (i < params.length); i++) {
                if (params[i] != parameters[i]) {
                    log.error("Hook method " + methodDescription + " does not match required parameters " + Arrays.asList(parameters));
                    return;
                }
            }
        }

        log.debug("Mapping hook for " + Arrays.toString(tags) + " to " + methodDescription);

        Class<?> hookClass = hookMethod.getDeclaringClass();
        Object hookObj = initializedClassesRegistry.get(hookClass.getName());

        if (hookObj == null) {
            hookObj = hookClass.getDeclaredConstructor().newInstance();
            kartaRuntime.initializeObject(hookObj);
            initializedClassesRegistry.add(hookObj);
        }

        for (String tag : tags) {
            if (!tagPatternMap.containsKey(tag)) {
                tagPatternMap.put(tag, Pattern.compile(tag));
            }

            Pattern tagPattern = tagPatternMap.get(tag);

            if (!taggedHooks.containsKey(tagPattern)) {
                taggedHooks.put(tagPattern, new ArrayList<>());
            }

            ArrayList<Pair<Object, Method>> hooksDef = taggedHooks.get(tagPattern);

            Pair<Object, Method> hookDefinition = new Pair<>(hookObj, hookMethod);

            hooksDef.add(hookDefinition);
        }

    }

    @Initializer
    public boolean initialize() throws Throwable {
        if (initialized) {
            return true;
        }
        log.info("Initializing " + PLUGIN_NAME + " plugin");

        if ((stepDefinitionPackageNames != null) && !stepDefinitionPackageNames.isEmpty()) {
            AnnotationScanner.forEachMethod(stepDefinitionPackageNames, StepDefinition.class, AnnotationScanner.IS_PUBLIC, null, null, this::processStepDefinitionMethod);
            AnnotationScanner.forEachMethod(stepDefinitionPackageNames, ChaosActionDefinition.class, AnnotationScanner.IS_PUBLIC, null, null, this::processChaosDefinitionMethod);
            AnnotationScanner.forEachMethod(stepDefinitionPackageNames, ConditionDefinition.class, AnnotationScanner.IS_PUBLIC, null, null, this::processConditionMethod);

            AnnotationScanner.forEachMethod(stepDefinitionPackageNames, BeforeRun.class, AnnotationScanner.IS_PUBLIC, null, null, processTaggedRunStartHook);
            AnnotationScanner.forEachMethod(stepDefinitionPackageNames, AfterRun.class, AnnotationScanner.IS_PUBLIC, null, null, processTaggedRunStopHook);
            AnnotationScanner.forEachMethod(stepDefinitionPackageNames, BeforeFeature.class, AnnotationScanner.IS_PUBLIC, null, null, processTaggedFeatureStartHook);
            AnnotationScanner.forEachMethod(stepDefinitionPackageNames, AfterFeature.class, AnnotationScanner.IS_PUBLIC, null, null, processTaggedFeatureStopHook);
            AnnotationScanner.forEachMethod(stepDefinitionPackageNames, BeforeScenario.class, AnnotationScanner.IS_PUBLIC, null, null, processTaggedScenarioStartHook);
            AnnotationScanner.forEachMethod(stepDefinitionPackageNames, AfterScenario.class, AnnotationScanner.IS_PUBLIC, null, null, processTaggedScenarioStopHook);
            AnnotationScanner.forEachMethod(stepDefinitionPackageNames, ScenarioFailed.class, AnnotationScanner.IS_PUBLIC, null, null, processTaggedScenarioFailedHook);
        } else {
            log.warn("No step definition packages found.");
        }
        initialized = true;
        return true;
    }

    @Override
    public TestFeature parseFeatureSource(String sourceString) throws Throwable {
        return ParserUtils.getYamlObjectMapper().readValue(sourceString, TestFeature.class);
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
    public StepResult runStep(PreparedStep testStep) throws TestFailureException {
        StepResult result = new StepResult();
        TestExecutionContext testExecutionContext = testStep.getTestExecutionContext();

        log.debug("Step run" + testStep);

        if (StringUtils.isBlank(testStep.getIdentifier())) {
            log.error("Empty step definition identifier for step " + testStep);
            return result;
        }

        String stepIdentifier = testStep.getIdentifier();

        // Fetch the positional argument names
        ArrayList<String> inlineStepDefinitionParameterNames = new ArrayList<>();
        Matcher matcher = testDataPattern.matcher(testStep.getIdentifier().trim());
        while (matcher.find()) {
            inlineStepDefinitionParameterNames.add(matcher.group());
        }

        stepIdentifier = sanitizeStepIdentifier(stepIdentifier);
        if (!stepHandlerMap.containsKey(stepIdentifier)) {
            // TODO: Handling undefined step to ask manual action(other configured handlers) if possible
            String errorMessage = "Missing step definition: " + stepIdentifier;
            log.error(errorMessage);
            StringBuilder positionalParameters = new StringBuilder();

            int i = 0;
            for (String inlineStepDefinitionParameterName : inlineStepDefinitionParameterNames) {
                positionalParameters.append(", Serializable posArg").append(i++).append(" /*= ").append(inlineStepDefinitionParameterName).append("*/");
            }
            log.error("Suggestion:\r\n   @StepDefinition( \"" + StringEscapeUtils.escapeJava(stepIdentifier) + "\" )\r\n" + "   public StepResult " + stepIdentifier.replaceAll(Constants.REGEX_WHITESPACE, Constants.UNDERSCORE) + "( TestExecutionContext context " + positionalParameters + ") throws Throwable\r\n" + "   {\r\n...\r\n   }");
            return StandardStepResults.error(errorMessage);
        }

        try {
            Pair<Object, Method> stepDefHandlerObjectMethodPair = stepHandlerMap.get(stepIdentifier);
            Object stepDefObject = stepDefHandlerObjectMethodPair.getLeft();
            Method stepDefMethodToInvoke = stepDefHandlerObjectMethodPair.getRight();
            BeanRegistry beanRegistry = testExecutionContext.getContextBeanRegistry();

            Object returnValue = runStepDefMethodWithParameters(testExecutionContext, inlineStepDefinitionParameterNames, stepDefMethodToInvoke, stepDefObject);

            Class<?> returnType = stepDefMethodToInvoke.getReturnType();

            StepDefinition stepDefinition = stepDefMethodToInvoke.getAnnotation(StepDefinition.class);
            StepOutputType stepOutputType = StepOutputType.AUTO_RESOLVE;
            String outputName = null;

            if (stepDefinition != null) {
                stepOutputType = stepDefinition.outputType();
                outputName = stepDefinition.outputName();
            }

            result = extractAndProcessStepResult(result, beanRegistry, returnType, returnValue, stepOutputType, outputName);
        } catch (Throwable t) {
            String errorMessage = "Exception occurred while running step definition " + testStep;
            log.error(errorMessage, t);
            result = StandardStepResults.error(errorMessage, t);
        }

        result.setEndTime(new Date());
        return result;
    }

    public Object runStepDefMethodWithParameters(TestExecutionContext testExecutionContext, ArrayList<String> inlineParameterNames, Method methodToInvoke, Object methodDefiningClassObject) throws JsonProcessingException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        HashMap<String, Serializable> testData = testExecutionContext.getData();
        HashMap<String, Serializable> variables = testExecutionContext.getVariables();

        Parameter[] parametersObj = methodToInvoke.getParameters();

        BeanRegistry beanRegistry = testExecutionContext.getContextBeanRegistry();

        ArrayList<Object> values = new ArrayList<>();

        for (int i = 0, positionalArg = 0; i < parametersObj.length; i++) {
            Class<?> paramType = parametersObj[i].getType();
            if (paramType == TestExecutionContext.class) {
                values.add(testExecutionContext);
                continue;
            }

            TestData testDataAnnotation = parametersObj[i].getAnnotation(TestData.class);
            ContextBean contextBeanAnnotation = parametersObj[i].getAnnotation(ContextBean.class);
            ContextVariable contextVariableAnnotation = parametersObj[i].getAnnotation(ContextVariable.class);

            JavaType typeToConvertTo = objectMapper.getTypeFactory().constructType(parametersObj[i].getParameterizedType());

            if (testDataAnnotation != null) {
                values.add(objectMapper.convertValue(testData.get(testDataAnnotation.value()), typeToConvertTo));
            } else if (contextBeanAnnotation != null) {
                if (beanRegistry != null) {
                    values.add(beanRegistry.get(contextBeanAnnotation.value()));
                } else {
                    values.add(null);
                }
            } else if (contextVariableAnnotation != null) {
                values.add(objectMapper.convertValue(variables.get(contextVariableAnnotation.value()), typeToConvertTo));
            } else {
                values.add(objectMapper.readValue(inlineParameterNames.get(positionalArg++), typeToConvertTo));
            }
        }

        return values.isEmpty() ? methodToInvoke.invoke(methodDefiningClassObject) : methodToInvoke.invoke(methodDefiningClassObject, values.toArray());
    }

    private StepResult extractAndProcessStepResult(StepResult result, BeanRegistry beanRegistry, Class<?> returnType, Object returnValue, StepOutputType stepOutputType, String outputName) {
        switch (stepOutputType) {
            case STEP_RESULT:
                if (returnType.equals(StepResult.class)) {
                    result = (StepResult) returnValue;
                } else {
                    result.setSuccessful(true);
                }
                break;

            case NONE:
                result.setSuccessful(true);
                break;

            case VARIABLE:
                result.setSuccessful(true);

                if (!StringUtils.isBlank(outputName) && Serializable.class.isAssignableFrom(returnType)) {
                    result.getResults().put(outputName, (Serializable) returnValue);
                }

                break;

            case BEAN:
                result.setSuccessful(true);
                if (!StringUtils.isBlank(outputName) && !Void.class.isAssignableFrom(returnType)) {
                    beanRegistry.put(outputName, returnValue);
                }
                break;

            case BOOLEAN:
                if (boolean.class.isAssignableFrom(returnType)) {
                    result = StepResult.builder().successful((boolean) returnValue).build();
                } else {
                    result.setSuccessful(true);
                }
                break;

            case AUTO_RESOLVE:
            default:
                if (returnType.equals(StepResult.class)) {
                    result = (StepResult) returnValue;
                } else if (!StringUtils.isBlank(outputName) && !Void.class.isAssignableFrom(returnType)) {
                    result.setSuccessful(true);

                    if (Serializable.class.isAssignableFrom(returnType)) {
                        result.getResults().put(outputName, (Serializable) returnValue);
                    } else {
                        beanRegistry.put(outputName, returnValue);
                    }
                } else if (boolean.class.isAssignableFrom(returnType)) {
                    result = StepResult.builder().successful((boolean) returnValue).build();
                } else {
                    result.setSuccessful(true);
                }
                break;
        }
        return result;
    }

    @Override
    public StepResult performChaosAction(PreparedChaosAction preparedChaosAction) {
        StepResult result = new StepResult();

        TestExecutionContext testExecutionContext = preparedChaosAction.getTestExecutionContext();
        HashMap<String, Serializable> testData = testExecutionContext.getData();
        HashMap<String, Serializable> variables = testExecutionContext.getVariables();

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

            Pair<Object, Method> chaosActionHandlerObjectMethodPair = chaosActionHandlerMap.get(chaosActionName);

            Object chaosActionHandlerObject = chaosActionHandlerObjectMethodPair.getLeft();
            Method chaosActionHandlerMethodToInvoke = chaosActionHandlerObjectMethodPair.getRight();

            Parameter[] parametersObj = chaosActionHandlerMethodToInvoke.getParameters();
            ArrayList<Object> values = new ArrayList<>();
            BeanRegistry beanRegistry = testExecutionContext.getContextBeanRegistry();

            for (Parameter parameter : parametersObj) {
                Class<?> paramType = parameter.getType();

                if (paramType == TestExecutionContext.class) {
                    values.add(testExecutionContext);
                } else if (paramType == PreparedChaosAction.class) {
                    values.add(preparedChaosAction);
                } else {
                    TestData testDataAnnotation = parameter.getAnnotation(TestData.class);
                    ContextBean contextBeanAnnotation = parameter.getAnnotation(ContextBean.class);
                    ContextVariable contextVariableAnnotation = parameter.getAnnotation(ContextVariable.class);

                    JavaType typeToConvertTo = objectMapper.getTypeFactory().constructType(parameter.getParameterizedType());

                    if (testDataAnnotation != null) {
                        values.add(objectMapper.convertValue(testData.get(testDataAnnotation.value()), typeToConvertTo));
                    } else if (contextBeanAnnotation != null) {
                        if (beanRegistry != null) {
                            values.add(beanRegistry.get(contextBeanAnnotation.value()));
                        } else {
                            values.add(null);
                        }
                    } else if (contextVariableAnnotation != null) {
                        values.add(objectMapper.convertValue(variables.get(contextVariableAnnotation.value()), typeToConvertTo));
                    }
                }

                Class<?> returnType = chaosActionHandlerMethodToInvoke.getReturnType();
                Object returnValue = chaosActionHandlerMethodToInvoke.invoke(chaosActionHandlerObject, values.toArray());

                ChaosActionDefinition chaosActionDefinition = chaosActionHandlerMethodToInvoke.getAnnotation(ChaosActionDefinition.class);
                StepOutputType stepOutputType = StepOutputType.AUTO_RESOLVE;
                String outputName = null;

                if (chaosActionDefinition != null) {
                    stepOutputType = chaosActionDefinition.outputType();
                    outputName = chaosActionDefinition.outputName();
                }

                result = extractAndProcessStepResult(result, beanRegistry, returnType, returnValue, stepOutputType, outputName);
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
    public boolean runCondition(TestExecutionContext testExecutionContext, String conditionIdentifier) {
        // TestExecutionContext testExecutionContext = testStep.getTestExecutionContext();

        log.debug("Condition check" + conditionIdentifier);

        if (StringUtils.isBlank(conditionIdentifier)) {
            log.error("Empty condition definition identifier " + conditionIdentifier);
            return false;
        }

        // Fetch the positional argument names
        ArrayList<String> inlineStepDefinitionParameterNames = new ArrayList<>();
        Matcher matcher = testDataPattern.matcher(conditionIdentifier.trim());
        while (matcher.find()) {
            inlineStepDefinitionParameterNames.add(matcher.group());
        }

        conditionIdentifier = sanitizeStepIdentifier(conditionIdentifier);
        if (!conditionDefinitionMap.containsKey(conditionIdentifier)) {
            // TODO: Handling undefined step to ask manual action(other configured handlers) if possible
            String errorMessage = "Missing condition definition: " + conditionIdentifier;
            log.error(errorMessage);
            StringBuilder positionalParameters = new StringBuilder();

            int i = 0;
            for (String inlineConditionDefinitionParameterName : inlineStepDefinitionParameterNames) {
                positionalParameters.append(", Serializable posArg").append(i++).append(" /*= ").append(inlineConditionDefinitionParameterName).append("*/");
            }
            log.error("Suggestion:\r\n   @ConditionDefinition( \"" + StringEscapeUtils.escapeJava(conditionIdentifier) + "\" )\r\n" + "   public boolean " + conditionIdentifier.replaceAll(Constants.REGEX_WHITESPACE, Constants.UNDERSCORE) + "(  " + positionalParameters + ") throws Throwable\r\n" + "   {\r\n...\r\n   }");
            return false;
        }

        try {
            Pair<Object, Method> conditionHandlerObjectMethodPair = conditionDefinitionMap.get(conditionIdentifier);
            Object conditionDefObject = conditionHandlerObjectMethodPair.getLeft();
            Method conditionDefMethodToInvoke = conditionHandlerObjectMethodPair.getRight();
            Class<?> returnType = conditionDefMethodToInvoke.getReturnType();

            if ((returnType != boolean.class) && (returnType != Boolean.class)) {
                return false;
            }

            return (boolean) (Boolean) runStepDefMethodWithParameters(testExecutionContext, inlineStepDefinitionParameterNames, conditionDefMethodToInvoke, conditionDefObject);
        } catch (Throwable t) {
            String errorMessage = "Exception occurred while running step definition " + conditionIdentifier;
            log.error(errorMessage, t);
            return false;
        }
    }

    public boolean invokeTaggedMethods(HashMap<Pattern, ArrayList<Pair<Object, Method>>> taggedHooksList, HashSet<String> tags, Object... parameters) {
        HashSet<Method> alreadyInvokedMethods = new HashSet<>();

        for (String tag : tags) {
            for (Entry<Pattern, ArrayList<Pair<Object, Method>>> patternHooksEntrySet : taggedHooksList.entrySet()) {
                Pattern tagPattern = patternHooksEntrySet.getKey();

                if (tagPattern.matcher(tag).matches()) {
                    for (Pair<Object, Method> objectMethodPair : patternHooksEntrySet.getValue()) {
                        Object hookObject = objectMethodPair.getLeft();
                        Method hookMethodToInvoke = objectMethodPair.getRight();

                        if (alreadyInvokedMethods.contains(hookMethodToInvoke)) {
                            // Already called feature start method for another tag
                            continue;
                        }

                        alreadyInvokedMethods.add(hookMethodToInvoke);

                        try {
                            hookMethodToInvoke.invoke(hookObject, parameters);
                        } catch (Throwable e) {
                            log.error("", e);
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean runStart(String runName, HashSet<String> tags) {
        if (tags != null) {
            return invokeTaggedMethods(taggedRunStartHooks, tags, runName);
        }
        return true;
    }

    @Override
    public boolean runStop(String runName, HashSet<String> tags) {
        if (tags != null) {
            return invokeTaggedMethods(taggedRunStopHooks, tags, runName);
        }
        return true;
    }

    @Override
    public boolean featureStart(String runName, TestFeature feature, HashSet<String> tags) {
        if (tags != null) {
            return invokeTaggedMethods(taggedFeatureStartHooks, tags, runName, feature);
        }
        return true;
    }

    @Override
    public boolean scenarioStart(String runName, String featureName, PreparedScenario scenario, HashSet<String> tags) {
        if (tags != null) {
            return invokeTaggedMethods(taggedScenarioStartHooks, tags, runName, featureName, scenario);
        }
        return true;
    }

    @Override
    public boolean scenarioStop(String runName, String featureName, PreparedScenario scenario, HashSet<String> tags) {
        if (tags != null) {
            return invokeTaggedMethods(taggedScenarioStopHooks, tags, runName, featureName, scenario);
        }
        return true;
    }

    @Override
    public boolean scenarioFailed(String runName, String featureName, PreparedScenario scenario, HashSet<String> tags, ScenarioResult scenarioResult) {
        if (tags != null) {
            return invokeTaggedMethods(taggedScenarioFailureHooks, tags, runName, featureName, scenario, scenarioResult);
        }
        return true;
    }

    @Override
    public boolean featureStop(String runName, TestFeature feature, HashSet<String> tags) {
        if (tags != null) {
            return invokeTaggedMethods(taggedFeatureStopHooks, tags, runName, feature);
        }
        return true;
    }

}
