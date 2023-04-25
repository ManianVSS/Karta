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
public class StepDefinition extends Step {
    private String fileName;
    private String name;
    private String className;


    @SuppressWarnings("unchecked")
    @Override
    public Object execute(Runner runner, Scope scope) {
        if (StringUtils.isNotBlank(fileName)) {
            try {
                runner.importStepDefMappingFromFile(fileName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(className)) {
            Class<?> stepDefClass;
            try {
                stepDefClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not find class in classpath: " + className);
            }

            if (!Step.class.isAssignableFrom(stepDefClass)) {
                throw new RuntimeException("Not a step definition class " + stepDefClass.getCanonicalName());
            }
            runner.getStepDefMapping().putIfAbsent(name, (Class<? extends Step>) stepDefClass);
        } else {
            throw new RuntimeException("Either step definition XML mapping file should be provided or name and className");
        }
        return this;
    }
}
