package com.github.kinetic.nixthing.ast;

public class NixParen extends NixExpression {
    private final NixExpression expression;

    public NixParen(NixExpression expression) {
        this.expression = expression;
    }

    public NixExpression getExpression() {
        return expression;
    }
}
