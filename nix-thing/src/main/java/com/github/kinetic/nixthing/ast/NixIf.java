package com.github.kinetic.nixthing.ast;

public final class NixIf extends NixExpression {
    private final NixExpression condition;
    private final NixExpression thenExpression;
    private final NixExpression elseExpression;

    public NixIf(final NixExpression condition, final NixExpression thenExpression, final NixExpression elseExpression) {
        this.condition = condition;
        this.thenExpression = thenExpression;
        this.elseExpression = elseExpression;
    }

    public NixExpression getCondition() {
        return condition;
    }

    public NixExpression getThenExpression() {
        return thenExpression;
    }

    public NixExpression getElseExpression() {
        return elseExpression;
    }
}
