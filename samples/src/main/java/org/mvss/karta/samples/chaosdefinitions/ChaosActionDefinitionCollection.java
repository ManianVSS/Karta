package org.mvss.karta.samples.chaosdefinitions;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.mvss.karta.framework.annotations.ChaosActionDefinition;
import org.mvss.karta.framework.models.test.PreparedChaosAction;
import org.mvss.karta.framework.utils.RandomizationUtils;

import java.util.Collection;
import java.util.Random;

@Log4j2
@NoArgsConstructor
public class ChaosActionDefinitionCollection {
    private final Random random = new Random();

    @ChaosActionDefinition("the calculator is powered off")
    public void the_calculator_is_powered_off(PreparedChaosAction actionToPerform) {
        Collection<String> subjects = RandomizationUtils.selectByChaos(random, actionToPerform.getSubjects(), actionToPerform.getChaos());
        log.info("Chaos action called " + actionToPerform + " actions to be performed on " + subjects);
    }
}
