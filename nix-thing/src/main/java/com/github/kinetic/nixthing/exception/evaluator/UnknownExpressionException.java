package com.github.kinetic.nixthing.exception.evaluator;

public final class UnknownExpressionException extends RuntimeException {

    public UnknownExpressionException(final String type) {
        super(String.format("Unknown expression type to evaluate: %s", type));
    }
}
