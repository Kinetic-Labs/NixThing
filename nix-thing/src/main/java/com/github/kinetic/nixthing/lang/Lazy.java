package com.github.kinetic.nixthing.lang;

import com.github.kinetic.nixthing.ast.NixExpression;
import com.github.kinetic.nixthing.core.enviornment.Environment;
import com.github.kinetic.nixthing.core.eval.Evaluator;

public final class Lazy {
    private final NixExpression expression;
    private final Environment environment;
    private NixExpression value;
    private boolean evaluated = false;
    private boolean evaluating = false;

    public Lazy(final NixExpression expression, final Environment environment) {
        this.expression = expression;
        this.environment = environment;
    }

    public static Lazy evaluated(final NixExpression value) {
        final Lazy lazy = new Lazy(value, null);

        lazy.value = value;
        lazy.evaluated = true;

        return lazy;
    }

    public NixExpression getValue() {
        if(evaluating)
            throw new RuntimeException("Infinite recursion detected in lazy value.");

        if(!evaluated) {
            try {
                this.evaluating = true;
                this.value = new Evaluator().eval(expression, environment);
                this.evaluated = true;
            } finally {
                this.evaluating = false;
            }
        }

        return value;
    }
}