package org.mvss.karta.xlang.steps.operations;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.xlang.dto.Scope;
import org.mvss.karta.xlang.runtime.Runner;

@Getter
@Setter
@ToString
public class Increment extends Operation {

    @Override
    public Object execute(Runner runner, Scope scope) throws Throwable {
        if (StringUtils.isBlank(resultVar) || !scope.hasLocalVariable(resultVar)) {
            throw new RuntimeException("Result variable missing");
        }

        Object existingVariable = scope.getVariable(resultVar);

        Object result;
        if (existingVariable instanceof Integer) {
            result = (Integer) existingVariable + 1;
            scope.putVariable(resultVar, result);
        } else if (existingVariable instanceof Long) {
            result = (Long) existingVariable + 1;
            scope.putVariable(resultVar, result);
        } else if (existingVariable instanceof Float) {
            result = (Float) existingVariable + 1;
            scope.putVariable(resultVar, result);
        } else if (existingVariable instanceof Double) {
            result = (Double) existingVariable + 1;
            scope.putVariable(resultVar, result);
        } else {
            throw new RuntimeException("Increment can't be performed on type " + existingVariable.getClass().getCanonicalName());
        }
        return result;
    }
}
