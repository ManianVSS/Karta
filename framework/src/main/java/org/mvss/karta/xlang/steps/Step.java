package org.mvss.karta.xlang.steps;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.mvss.karta.xlang.dto.Scope;
import org.mvss.karta.xlang.runtime.Runner;
import org.mvss.karta.xlang.steps.conditions.Condition;

import java.io.Serializable;
import java.util.ArrayList;

@Getter
@Setter
@ToString
public class Step implements Serializable {

    protected ArrayList<Step> steps = new ArrayList<>();

    public Object execute(Runner runner, Scope scope) throws Throwable {
        return runner.run(steps, scope);
    }

    protected boolean evaluateSingleCondition(Runner runner, Scope scope) throws Throwable {
        Step firstStep;

        if ((steps == null) || (steps.size() < 1) || !((firstStep = steps.get(0)) instanceof Condition)) {
            throw new RuntimeException("No condition provided");
        }
        return ((Condition) firstStep).eval(runner, scope);
    }
}
