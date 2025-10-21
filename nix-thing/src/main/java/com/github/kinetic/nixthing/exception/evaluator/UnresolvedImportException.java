package com.github.kinetic.nixthing.exception.evaluator;

public final class UnresolvedImportException extends RuntimeException {

    public UnresolvedImportException(final String cause) {
        super(String.format("Error occurred resolving import: %s", cause));
    }

    public UnresolvedImportException(final String file, final Exception cause) {
        super(String.format("Failed to read import file: %s", file), cause);
    }
}
