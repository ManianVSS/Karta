package org.mvss.karta.xlang.steps;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.mvss.karta.dependencyinjection.utils.RegexUtil;
import org.mvss.karta.xlang.dto.Scope;
import org.mvss.karta.xlang.runtime.Runner;

@Getter
@Setter
@ToString
public class Echo extends Step {
    private String message;

    @Override
    public Object execute(Runner runner, Scope scope) {
        System.out.println(RegexUtil.replaceVariables(scope::getVariable, message));
        return null;
    }
}
