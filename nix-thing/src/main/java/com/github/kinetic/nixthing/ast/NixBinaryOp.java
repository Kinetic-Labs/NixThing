package com.github.kinetic.nixthing.ast;

public final class NixBinaryOp extends NixExpression {

    private final NixExpression left;
    private final NixExpression right;
    private final String operator;

    public NixBinaryOp(final NixExpression left, final String operator, final NixExpression right) {
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public NixExpression getLeft() {
        return left;
    }

    public String getOperator() {
        return operator;
    }

    public NixExpression getRight() {
        return right;
    }
}
