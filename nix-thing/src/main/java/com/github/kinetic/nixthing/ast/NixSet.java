package com.github.kinetic.nixthing.ast;

import com.github.kinetic.nixthing.core.enviornment.Environment;

import java.util.List;

public final class NixSet extends NixExpression {

    private final List<NixBinding> bindings;
    private final Environment env;

    public NixSet(final List<NixBinding> bindings) {
        this.bindings = bindings;
        this.env = null;
    }

    public NixSet(final List<NixBinding> bindings, final Environment env) {
        this.bindings = bindings;
        this.env = env;
    }

    public List<NixBinding> getBindings() {
        return bindings;
    }

    public Environment getEnv() {
        return env;
    }
}