package com.github.kinetic.nixthing.ast;

import java.util.Objects;

public final class NixString extends NixExpression {

    private final String value;

    public NixString(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object object) {
        if(this == object)
            return true;

        if(object == null || getClass() != object.getClass())
            return false;

        final NixString nixString = (NixString) object;

        return Objects.equals(value, nixString.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
