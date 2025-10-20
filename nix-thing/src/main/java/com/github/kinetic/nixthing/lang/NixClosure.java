package com.github.kinetic.nixthing.lang;

import com.github.kinetic.nixthing.ast.NixExpression;
import com.github.kinetic.nixthing.ast.NixFunction;
import com.github.kinetic.nixthing.core.enviornment.Environment;

public final class NixClosure extends NixExpression {
    private final NixFunction function;
    private final Environment environment;

    public NixClosure(final NixFunction function, final Environment environment) {
        this.function = function;
        this.environment = environment;
    }

    public NixFunction getFunction() {
        return function;
    }

    public Environment getEnvironment() {
        return environment;
    }
}
