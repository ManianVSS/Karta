package org.mvss.karta.xlang.steps.conditions;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.mvss.karta.dependencyinjection.utils.RegexUtil;
import org.mvss.karta.xlang.dto.Scope;
import org.mvss.karta.xlang.runtime.Runner;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class NotEquals extends Condition {

    private String operand1;
    private String operand2;

    @Override
    public boolean eval(Runner runner, Scope scope) {
        Serializable op1Value = RegexUtil.replaceVariables(scope::getVariable, operand1);
        Serializable op2Value = RegexUtil.replaceVariables(scope::getVariable, operand2);
        return !op1Value.equals(op2Value);
    }
}
