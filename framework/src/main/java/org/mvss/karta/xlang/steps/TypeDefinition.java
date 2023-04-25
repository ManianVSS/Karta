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
public class TypeDefinition extends Step {

    private String fileName;
    private String name;
    private String className;


    @Override
    public Object execute(Runner runner, Scope scope) {
        if (StringUtils.isNotBlank(fileName)) {
            try {
                runner.importTypeDefMappingFromFile(fileName);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(className)) {
            Class<?> typeDefClass;
            try {
                typeDefClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not find class in classpath: " + className);
            }
            runner.getTypeMapping().putIfAbsent(name, typeDefClass);
        } else {
            throw new RuntimeException("Either type definition XML mapping file should be provided or name and className");
        }
        return this;
    }
}
