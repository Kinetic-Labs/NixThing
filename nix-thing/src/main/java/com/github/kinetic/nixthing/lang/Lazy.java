package com.github.kinetic.nixthing.lang;

import com.github.kinetic.nixthing.ast.NixExpression;
import com.github.kinetic.nixthing.core.Environment;
import com.github.kinetic.nixthing.core.Evaluator;

public class Lazy {
    private final NixExpression expression;
    private final Environment environment;
    private NixExpression value;
    private boolean evaluated = false;
    private boolean evaluating = false;

    public Lazy(NixExpression expression, Environment environment) {
        this.expression = expression;
        this.environment = environment;
    }

    public NixExpression getValue() {
        if (evaluating) {
            throw new RuntimeException("Infinite recursion detected in lazy value.");
        }
        if (!evaluated) {
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

    public static Lazy evaluated(NixExpression value) {
        Lazy lazy = new Lazy(value, null);
        lazy.value = value;
        lazy.evaluated = true;
        return lazy;
    }
}