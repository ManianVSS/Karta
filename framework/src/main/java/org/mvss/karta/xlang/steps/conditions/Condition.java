package org.mvss.karta.xlang.steps.conditions;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.xlang.dto.Scope;
import org.mvss.karta.xlang.runtime.Runner;
import org.mvss.karta.xlang.steps.operations.Operation;

@Getter
@Setter
@ToString
public abstract class Condition extends Operation {

    @Override
    public Boolean execute(Runner runner, Scope scope) throws Throwable {
        if (StringUtils.isNotBlank(resultVar)) {
            boolean result = eval(runner, scope);
            scope.putVariable(resultVar, result);
            return result;
        }
        return false;
    }

    public abstract boolean eval(Runner runner, Scope scope) throws Throwable;
}
