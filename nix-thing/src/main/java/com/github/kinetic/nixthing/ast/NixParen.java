package com.github.kinetic.nixthing.ast;

public final class NixParen extends NixExpression {
    private final NixExpression expression;

    public NixParen(final NixExpression expression) {
        this.expression = expression;
    }

    public NixExpression getExpression() {
        return expression;
    }
}
