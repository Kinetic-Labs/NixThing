package com.github.kinetic.nixthing.ast;

public class NixInherit extends NixExpression {
    private final NixIdentifier identifier;

    public NixInherit(NixIdentifier identifier) {
        this.identifier = identifier;
    }

    public NixIdentifier getIdentifier() {
        return identifier;
    }
}
