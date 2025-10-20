package com.github.kinetic.nixthing.ast;

import java.util.List;

public class NixLet extends NixExpression {
    private final List<NixBinding> bindings;
    private final NixExpression inExpression;

    public NixLet(List<NixBinding> bindings, NixExpression inExpression) {
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
