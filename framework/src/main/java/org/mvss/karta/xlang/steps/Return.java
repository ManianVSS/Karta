package org.mvss.karta.xlang.steps;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.mvss.karta.xlang.dto.Scope;
import org.mvss.karta.xlang.runtime.Runner;

@Getter
@Setter
@ToString
public class Return extends Step {

    protected String result;

    @Override
    public Object execute(Runner runner, Scope scope) throws Throwable {
        Object existingVariable = scope.getVariable(result);
        throw new FunctionCallReturnException(existingVariable);
    }
}
