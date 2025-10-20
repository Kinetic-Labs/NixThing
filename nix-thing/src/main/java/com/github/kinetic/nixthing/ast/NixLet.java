package com.github.kinetic.nixthing.ast;

import java.util.List;

public final class NixLet extends NixExpression {
    private final List<NixBinding> bindings;
    private final NixExpression inExpression;

    public NixLet(final List<NixBinding> bindings, final NixExpression inExpression) {
        this.bindings = bindings;
        this.inExpression = inExpression;
    }

    public List<NixBinding> getBindings() {
        return bindings;
    }

    public NixExpression getInExpression() {
        return inExpression;
    }
}
