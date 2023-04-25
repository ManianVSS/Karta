package org.mvss.karta.xlang.steps.operations;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.mvss.karta.xlang.dto.Scope;
import org.mvss.karta.xlang.runtime.Runner;
import org.mvss.karta.xlang.steps.Step;

@Getter
@Setter
@ToString
public abstract class Operation extends Step {
    protected String resultVar;

    @Override
    public abstract Object execute(Runner runner, Scope scope) throws Throwable;
}
