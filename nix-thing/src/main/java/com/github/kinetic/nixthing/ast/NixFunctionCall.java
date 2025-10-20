package com.github.kinetic.nixthing.ast;

public class NixFunctionCall extends NixExpression {
    private final NixExpression function;
    private final NixExpression argument;

    public NixFunctionCall(NixExpression function, NixExpression argument) {
        this.function = function;
        this.argument = argument;
    }

    public NixExpression getFunction() {
        return function;
    }

    public NixExpression getArgument() {
        return argument;
    }
}
