package org.mvss.karta.samples.stepdefinitions;

import lombok.extern.log4j.Log4j2;
import org.mvss.karta.framework.annotations.ContextBean;
import org.mvss.karta.framework.annotations.ContextVariable;
import org.mvss.karta.framework.annotations.StepDefinition;
import org.mvss.karta.framework.annotations.TestData;
import org.mvss.karta.framework.models.result.StepResult;
import org.mvss.karta.framework.models.run.TestExecutionContext;
import org.mvss.karta.framework.utils.RandomizationUtils;

import javax.xml.bind.DatatypeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

@Log4j2
public class StepDefinitionsCollection2 {
    private final Random random = new Random();
    private volatile String token = RandomizationUtils.randomAlphaNumericString(random, 10);

    @StepDefinition("a binary operation is perfomed on the calculator")
    public StepResult a_binary_operation_is_performed_on_the_calculator(TestExecutionContext context, @TestData("operand2") Integer intRange,
                                                                        @TestData("operand3") byte[] byteRange) {
        log.info("a binary operation is performed on the calculator with token " + token + " and  data: " + context.getTestData());

        if (byteRange != null) {
            log.info("Operand 3 is " + DatatypeConverter.printHexBinary(byteRange));
        }

        HashMap<String, Serializable> results = new HashMap<>();
        if ((intRange == null) || (intRange == 0)) {
            intRange = Integer.MAX_VALUE;
        }
        results.put("BinaryOperationResult", random.nextInt(intRange));
        return StepResult.builder().successful(true).results(results).build();
    }

    @StepDefinition(value = "authenticate to get new token")
    public void authenticate_to_get_new_token(TestExecutionContext context, @TestData("userName") String userName,
                                              @TestData("password") String password) {
        log.info("authenticating with username " + userName + " and password " + password + " . Test data passed is" + context.getTestData());
        String oldToken = token;
        token = RandomizationUtils.randomAlphaNumericString(random, 10);
        log.info("new token: " + token);
        context.getContextData().put("token", token);
        context.getContextData().put("oldToken", oldToken);
    }

    @StepDefinition("reserve use of calculator for next \"\" milliseconds")
    public void reserve_use_of_calculator_for_next_milliseconds(TestExecutionContext context, long seconds) {
        log.info("reserving calculator for " + seconds + " seconds. Test data passed is" + context.getTestData());
    }

    @StepDefinition(value = "logoff the old token")
    public void logoff_the_old_token(TestExecutionContext context, @ContextBean(value = "nullBeanDemo") String nullBean,
                                     @ContextVariable(value = "oldToken") String oldToken) {
        log.info("Checking null bean: " + nullBean);
        log.info("Logging off old token: " + oldToken);
    }
}
