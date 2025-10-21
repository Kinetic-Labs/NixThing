package com.github.kinetic.nixthing.ast;

public final class NixFunctionCall extends NixExpression {

    private final NixExpression function;
    private final NixExpression argument;

    public NixFunctionCall(final NixExpression function, final NixExpression argument) {
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
