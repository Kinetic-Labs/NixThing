package com.github.kinetic.nixthing.ast;

import java.util.Objects;

public class NixIdentifier extends NixExpression {
    private final String name;

    public NixIdentifier(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NixIdentifier that = (NixIdentifier) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
