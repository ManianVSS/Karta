package org.mvss.karta.xlang.steps;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.dependencyinjection.utils.RegexUtil;
import org.mvss.karta.xlang.dto.Scope;
import org.mvss.karta.xlang.runtime.Runner;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@ToString
public class VariableDefinition extends Step {

    public static final ConcurrentHashMap<String, Class<?>> typeMap = new ConcurrentHashMap<>();

    private String type;
    private String name;
    private Serializable value;

    private String expr;

    @Override
    public Object execute(Runner runner, Scope scope) {
        System.out.println("Variable Definition: " + this);
        if (!StringUtils.isBlank(expr)) {
            value = RegexUtil.replaceVariables(scope.getVariables(), expr);
        }
        scope.getVariables().put(name, value);
        return this;
    }
}
