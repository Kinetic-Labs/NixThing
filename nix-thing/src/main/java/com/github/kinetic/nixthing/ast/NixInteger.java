package com.github.kinetic.nixthing.ast;

public class NixInteger extends NixExpression {
    private final int value;

    public NixInteger(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NixInteger that = (NixInteger) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }
}
