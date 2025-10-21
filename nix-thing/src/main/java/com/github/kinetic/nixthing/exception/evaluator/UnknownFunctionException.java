package com.github.kinetic.nixthing.exception.evaluator;


public final class UnknownFunctionException extends RuntimeException {

    public UnknownFunctionException(final String name, final String type) {
        super(String.format("Failed to find function: %s of type: %s", name, type));
    }

    public UnknownFunctionException(final String name, final String type, final String tip) {
        super(String.format("Failed to find function: %s of type: %s, %s", name, type, tip));
    }
}
