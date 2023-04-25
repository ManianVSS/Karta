package org.mvss.karta.xlang.steps;

import lombok.Getter;

@Getter
public class FunctionCallReturnException extends Exception {
    private final Object returnValue;

    public FunctionCallReturnException(Object returnValue) {
        super("");
        this.returnValue = returnValue;
    }
}
