package org.mvss.karta.xlang.steps;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.mvss.karta.xlang.dto.Scope;
import org.mvss.karta.xlang.runtime.Runner;
import org.mvss.karta.xlang.steps.conditions.Condition;

@Getter
@Setter
@ToString
public class ForStatement extends Step {

    @Override
    public Object execute(Runner runner, Scope scope) throws Throwable {
        Step initStep;
        Step conditionStep;
        Step updateStep;
        Step doStep;

        if ((steps.size() != 4) || !((initStep = steps.get(0)) instanceof Init) || !((conditionStep = steps.get(1)) instanceof Condition) || !((updateStep = steps.get(2)) instanceof Update) || !((doStep = steps.get(3)) instanceof Do)) {
            throw new RuntimeException("For syntax error missing or misplaced init/<condition>/update/do block");
        }
        Scope localScope = new Scope();
        localScope.setParentScope(scope);

        for (initStep.execute(runner, localScope); ((Condition) conditionStep).eval(runner, localScope); updateStep.execute(runner, localScope)

        ) {
            try {
                doStep.execute(runner, localScope);
            } catch (ForStatementBreakException breakException) {
                break;
            } catch (ForStatementContinueException continueException) {
                //continue;
            }
        }
        return null;
    }

    public static class Init extends Step {

    }

    public static class Do extends Step {

    }

    public static class Update extends Step {

    }

    public static class ForStatementBreakException extends Exception {
        public ForStatementBreakException() {
            super("");
        }
    }

    public static class Break extends Step {

        @Override
        public Object execute(Runner runner, Scope scope) throws Throwable {
            throw new ForStatementBreakException();
        }
    }

    public static class ForStatementContinueException extends Exception {
        public ForStatementContinueException() {
            super("");
        }
    }

    public static class Continue extends Step {

        @Override
        public Object execute(Runner runner, Scope scope) throws Throwable {
            throw new ForStatementContinueException();
        }
    }
}
