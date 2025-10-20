package com.github.kinetic.nixthing.ast;

public final class NixInteger extends NixExpression {
    private final int value;

    public NixInteger(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object object) {
        if(this == object)
            return true;

        if(object == null || getClass() != object.getClass())
            return false;

        NixInteger that = (NixInteger) object;

        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}
