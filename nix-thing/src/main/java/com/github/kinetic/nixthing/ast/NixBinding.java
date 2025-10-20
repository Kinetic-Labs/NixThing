package com.github.kinetic.nixthing.ast;

public class NixBinding {
    private final NixIdentifier name;
    private final NixExpression value;

    public NixBinding(NixIdentifier name, NixExpression value) {
        this.name = name;
        this.value = value;
    }

    public NixIdentifier getName() {
        return name;
    }

    public NixExpression getValue() {
        return value;
    }
}
