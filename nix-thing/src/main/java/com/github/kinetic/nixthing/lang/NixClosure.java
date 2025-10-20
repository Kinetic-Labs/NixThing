package com.github.kinetic.nixthing.lang;

import com.github.kinetic.nixthing.ast.NixExpression;
import com.github.kinetic.nixthing.ast.NixFunction;
import com.github.kinetic.nixthing.core.Environment;

public class NixClosure extends NixExpression {
    private final NixFunction function;
    private final Environment environment;

    public NixClosure(NixFunction function, Environment environment) {
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
