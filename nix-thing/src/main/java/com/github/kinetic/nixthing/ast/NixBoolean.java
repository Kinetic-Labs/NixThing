package com.github.kinetic.nixthing.ast;

public final class NixBoolean extends NixExpression {

    private final boolean value;

    public NixBoolean(final boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object object) {
        if(this == object)
            return true;

        if(object == null || getClass() != object.getClass())
            return false;

        final NixBoolean that = (NixBoolean) object;

        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }
}
