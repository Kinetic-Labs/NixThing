package com.github.kinetic.nixthing.exception.evaluator;

public final class InheritErrorException extends RuntimeException {

    public static class InheritScopeErrorException extends RuntimeException {
        public InheritScopeErrorException(final String variable) {
            super(String.format("Inherited variable '%s' not found in scope.", variable));
        }
    }

    public static class InheritTopLevelScopeErrorException extends RuntimeException {
        public InheritTopLevelScopeErrorException(final String variable) {
            super(String.format("Inherited variable '%s' not found in scope.", variable));
        }
    }
}
