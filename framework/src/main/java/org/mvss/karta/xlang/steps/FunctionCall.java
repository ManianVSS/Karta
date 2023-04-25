package org.mvss.karta.xlang.steps;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.mvss.karta.xlang.dto.Scope;
import org.mvss.karta.xlang.runtime.Runner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@ToString
public class FunctionCall extends Step {

    private String name;

    private HashMap<String, Serializable> inputParameters;

    private ArrayList<String> outputParameters;

    @Override
    public Object execute(Runner runner, Scope scope) throws Throwable {
        System.out.println("Function call: " + this);

        if (!scope.getFunctions().containsKey(name)) {
            throw new RuntimeException("Could not find function named: " + name);
        }
        Scope functionScope = new Scope();
        functionScope.setParentScope(scope);

        if (inputParameters != null) {
            functionScope.getVariables().putAll(inputParameters);
        }

        ArrayList<Step> functionStep = scope.getFunctions().get(name);
        Object returnValue = runner.run(functionStep, functionScope);

        //Copy back output parameters to parent scope
        if ((outputParameters != null) && !outputParameters.isEmpty()) {
            ConcurrentHashMap<String, Object> scopeVariables = scope.getVariables();
            for (String outputParameter : outputParameters) {
                if (functionScope.hasLocalVariable(outputParameter)) {
                    scopeVariables.put(outputParameter, functionScope.getVariable(outputParameter));
                }
            }
        }
        return returnValue;
    }
}
