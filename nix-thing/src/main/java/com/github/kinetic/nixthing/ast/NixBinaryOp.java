package com.github.kinetic.nixthing.ast;

public class NixBinaryOp extends NixExpression {
    private final NixExpression left;
    private final NixExpression right;
    private final String operator;

    public NixBinaryOp(NixExpression left, String operator, NixExpression right) {
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
