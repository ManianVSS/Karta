package org.mvss.karta.xlang.steps;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.mvss.karta.xlang.dto.Scope;
import org.mvss.karta.xlang.runtime.Runner;

@Getter
@Setter
@ToString
public class Return extends Step {

    protected String result;

    @Override
    public Object execute(Runner runner, Scope scope) throws Throwable {
        Object existingVariable = StringUtils.isNotBlank(result) ? scope.getVariable(result) : null;
        throw new FunctionCallReturnException(existingVariable);
    }
}
