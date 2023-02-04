package org.mvss.karta.samples.stepdefinitions;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.framework.annotations.StepDefinition;
import org.mvss.karta.framework.models.result.StepResult;

@Log4j2
public class ErrorHandlingTestStepDefinition {
    @SuppressWarnings("ConstantConditions")
    @StepDefinition("throw a null pointer exception")
    public StepResult throw_a_null_poiter_exception() {
        String str = null;
        int inta = Integer.parseInt(str);
        log.info("Parsed int is " + inta);
        throw new NullPointerException();
    }

    @StepDefinition("continue with teardown even on exception")
    public void continue_with_teardown_even_on_exception() {
        log.info("Continuing with teardown even on exception...");
    }
}
