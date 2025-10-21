package com.github.kinetic.nixthing.ast;

import java.util.Objects;

public final class NixIdentifier extends NixExpression {

    private final String name;

    public NixIdentifier(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object object) {
        if(this == object)
            return true;

        if(object == null || getClass() != object.getClass())
            return false;

        NixIdentifier that = (NixIdentifier) object;

        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
