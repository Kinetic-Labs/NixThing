package com.github.kinetic.nixthing.ast;

public final class NixInherit extends NixExpression {

    private final NixIdentifier identifier;

    public NixInherit(final NixIdentifier identifier) {
        this.identifier = identifier;
    }

    public NixIdentifier getIdentifier() {
        return identifier;
    }
}
