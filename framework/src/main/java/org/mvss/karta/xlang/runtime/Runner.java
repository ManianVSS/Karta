package org.mvss.karta.xlang.runtime;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Getter;
import lombok.Setter;
import org.mvss.karta.dependencyinjection.interfaces.DependencyInjector;
import org.mvss.karta.dependencyinjection.utils.ClassPathLoaderUtils;
import org.mvss.karta.dependencyinjection.utils.XMLParser;
import org.mvss.karta.xlang.dto.Scope;
import org.mvss.karta.xlang.steps.*;
import org.mvss.karta.xlang.steps.conditions.Condition;
import org.mvss.karta.xlang.steps.conditions.Equals;
import org.mvss.karta.xlang.steps.conditions.NotEquals;
import org.mvss.karta.xlang.steps.operations.Increment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Runner implements AutoCloseable {

    public static final String STEP_MAPPING_XML = "StepMapping.xml";

    public static final ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());

    static {
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    private final HashMap<String, Class<? extends Step>> stepDefMapping = new HashMap<>();

    public static final ConcurrentHashMap<String, Class<?>> typeMapping = new ConcurrentHashMap<>();

    @Getter
    private final Scope scope;

    @Setter
    private DependencyInjector dependencyInjector;

    public Runner() {

        //Built-ins
        stepDefMapping.put("var", VariableDefinition.class);
        stepDefMapping.put("func", FunctionDefinition.class);
        stepDefMapping.put("call", FunctionCall.class);
        stepDefMapping.put("return", Return.class);
        stepDefMapping.put("import", Import.class);
        stepDefMapping.put("echo", Echo.class);

        stepDefMapping.put("if", IfStatement.class);
        stepDefMapping.put("then", IfStatement.Then.class);
        stepDefMapping.put("elseif", IfStatement.ElseIf.class);
        stepDefMapping.put("else", IfStatement.Else.class);

        stepDefMapping.put("for", ForStatement.class);
        stepDefMapping.put("init", ForStatement.Init.class);
        stepDefMapping.put("update", ForStatement.Update.class);
        stepDefMapping.put("do", ForStatement.Do.class);

        stepDefMapping.put("increment", Increment.class);

        stepDefMapping.put("equals", Equals.class);
        stepDefMapping.put("notequals", NotEquals.class);

        typeMapping.put("boolean", Boolean.class);
        typeMapping.put("byte", Byte.class);
        typeMapping.put("char", Character.class);
        typeMapping.put("short", Short.class);
        typeMapping.put("int", Integer.class);
        typeMapping.put("long", Long.class);
        typeMapping.put("float", Float.class);
        typeMapping.put("double", Double.class);
        typeMapping.put("string", String.class);

        typeMapping.put("boolean array", boolean[].class);
        typeMapping.put("byte array", byte[].class);
        typeMapping.put("char array", char[].class);
        typeMapping.put("short array", short[].class);
        typeMapping.put("int array", int[].class);
        typeMapping.put("long array", long[].class);
        typeMapping.put("float array", float[].class);
        typeMapping.put("double array", double[].class);
        typeMapping.put("string array", String[].class);

        try {
            String mappingFileContents = ClassPathLoaderUtils.readAllText(STEP_MAPPING_XML);
            //noinspection unchecked
            HashMap<String, Serializable> stepDefImplNameMapping = (HashMap<String, Serializable>) XMLParser.readObject(mappingFileContents);

            for (String key : stepDefImplNameMapping.keySet()) {

                if (stepDefMapping.containsKey(key)) {
                    throw new RuntimeException("Step definition already mapped for: " + key + " to " + stepDefMapping.get(key).getCanonicalName());
                }

                String value = (String) stepDefImplNameMapping.get(key);
                //noinspection unchecked
                stepDefMapping.put(key, (Class<? extends Step>) Class.forName(value));
            }
            scope = new Scope();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<Condition> getConditions(List<Step> steps) {
        ArrayList<Condition> conditions = null;

        if (steps != null) {
            conditions = new ArrayList<>();
            for (Step step : steps) {
                if (step instanceof Condition) {
                    conditions.add((Condition) step);
                }
            }
        }

        return conditions;
    }

    public ArrayList<Step> getSteps(Element element) throws Throwable {
        ArrayList<Step> steps = new ArrayList<>();
        NodeList stepNodeList = element.getChildNodes();

        for (int itr = 0; itr < stepNodeList.getLength(); itr++) {
            Node stepNode = stepNodeList.item(itr);
            stepNode.normalize();
            // Attributes somehow don't make it in getChildNodes to switch case hence separate loop.
            if (stepNode.getNodeType() == Node.ELEMENT_NODE) {
                Element stepElement = (Element) stepNode;
//                    parsedObject.put(stepElement.getNodeName(), readObject(stepElement));
                String step = stepElement.getNodeName();

                if (!stepDefMapping.containsKey(step)) {
                    throw new RuntimeException("Could not find implementation for step type: " + step);
                }

                Class<? extends Step> stepDefClass = stepDefMapping.get(step);
                Serializable objectRead = XMLParser.readAttributesAsObject(stepElement);
                Step stepObjectRead = objectMapper.convertValue(objectRead, stepDefClass);
                stepObjectRead.setSteps(getSteps(stepElement));
                if (dependencyInjector != null) {
                    dependencyInjector.injectIntoObject(stepObjectRead);
                }
                steps.add(stepObjectRead);
            }
        }
        return steps;
    }

    public Object run(String xmlFileName) throws Throwable {
        return run(xmlFileName, this.scope);
    }

    public Object run(String xmlFileName, Scope scope) throws Throwable {
        Document doc = XMLParser.readDocumentFromFile(xmlFileName);
        Element rootElement = doc.getDocumentElement();
        rootElement.normalize();
        return run(rootElement, scope);
    }

    public Object run(Element element) throws Throwable {
        return run(element, this.scope);
    }

    public Object run(Element element, Scope scope) throws Throwable {
        return run(getSteps(element), scope);
    }

    public Object run(List<Step> steps) throws Throwable {
        return run(steps, this.scope);
    }

    public Object run(List<Step> steps, Scope scope) throws Throwable {
        ArrayList<Object> listOfObjects = new ArrayList<>();
        try {

            for (Step step : steps) {
                listOfObjects.add(step.execute(this, scope));
            }

        } catch (FunctionCallReturnException functionCallReturnException) {
            Object returnValue = functionCallReturnException.getReturnValue();
            if (returnValue != null) {
                return returnValue;
            }
        }
        return listOfObjects.size() == 1 ? listOfObjects.get(0) : listOfObjects;
    }

    public Object importFile(String xmlFileName, Scope scope) throws Throwable {
        Document doc = XMLParser.readDocumentFromFile(xmlFileName);
        Element rootElement = doc.getDocumentElement();
        rootElement.normalize();
        return run(rootElement, scope);
    }

    @Override
    public void close() throws Exception {
        scope.close();
    }
}
