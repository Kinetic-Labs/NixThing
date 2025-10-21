package com.github.kinetic.nixthing.exception.evaluator;

import com.github.kinetic.nixthing.ast.NixExpression;

public final class InvalidBinaryOperationException extends RuntimeException {

    public InvalidBinaryOperationException(final String operator, final NixExpression left, final NixExpression right) {
        super(String.format("Cannot apply operator '%s' to '%s' and '%s'", operator, left, right));
    }
}
