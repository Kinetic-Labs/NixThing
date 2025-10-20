package com.github.kinetic.nixthing.ast;

public class NixIf extends NixExpression {
    private final NixExpression condition;
    private final NixExpression thenExpression;
    private final NixExpression elseExpression;

    public NixIf(NixExpression condition, NixExpression thenExpression, NixExpression elseExpression) {
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
