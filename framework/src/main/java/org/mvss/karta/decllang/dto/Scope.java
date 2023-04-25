package org.mvss.karta.decllang.dto;

import lombok.*;
import org.mvss.karta.decllang.models.Step;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Scope {
    private Scope parentScope;
    @Builder.Default
    private ConcurrentHashMap<String, Serializable> variables = new ConcurrentHashMap<>();
    @Builder.Default
    private ConcurrentHashMap<String, ArrayList<Step>> functions = new ConcurrentHashMap<>();

    public boolean hasLocalVariable(String name) {
        return variables.containsKey(name);
    }

    public Serializable getVariable(String name) {
        if (variables.containsKey(name)) {
            return variables.get(name);
        } else if (parentScope != null) {
            return parentScope.getVariable(name);
        } else {
            return null;
        }
    }

    public void putVariable(String name, Serializable value) {
        variables.put(name, value);
    }

    public boolean hasLocalFunction(String name) {
        return functions.containsKey(name);
    }

    public ArrayList<Step> getFunction(String name) {
        if (functions.containsKey(name)) {
            return functions.get(name);
        } else if (parentScope != null) {
            return parentScope.getFunction(name);
        } else {
            return null;
        }
    }

    public void putFunction(String name, ArrayList<Step> steps) {
        functions.put(name, steps);
    }
}
