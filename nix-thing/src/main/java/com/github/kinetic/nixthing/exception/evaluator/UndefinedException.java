package com.github.kinetic.nixthing.exception.evaluator;

public class UndefinedException extends RuntimeException {

    public UndefinedException(final String variable) {
        super(String.format("Undefined variable: %s", variable));
    }
}
