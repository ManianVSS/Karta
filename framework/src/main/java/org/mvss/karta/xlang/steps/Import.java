package org.mvss.karta.xlang.steps;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.mvss.karta.xlang.dto.Scope;
import org.mvss.karta.xlang.runtime.Runner;

@Getter
@Setter
@ToString
public class Import extends Step {
    private String fileName;

    @Override
    public Object execute(Runner runner, Scope scope) throws Throwable {
        return runner.importFile(fileName, scope);
    }
}
