package com.github.kinetic.nixthing.ast;

import java.util.Objects;

public class NixString extends NixExpression {
    private final String value;

    public NixString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NixString nixString = (NixString) o;
        return Objects.equals(value, nixString.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
