package com.github.kinetic.nixthing.core;

import com.github.kinetic.nixthing.ast.NixExpression;
import com.github.kinetic.nixthing.lang.Lazy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Environment {

    private final Environment parent;
    private final Map<String, Lazy> variables = new HashMap<>();

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public Environment getParent() {
        return parent;
    }

    public Map<String, Lazy> getVariables() {
        return variables;
    }

    public void define(String name, NixExpression value, Environment env) {
        variables.put(name, new Lazy(value, env));
    }

    public void defineEvaluated(String name, NixExpression value) {
        variables.put(name, Lazy.evaluated(value));
    }

    public Optional<NixExpression> lookup(String name) {
        if (variables.containsKey(name)) {
            return Optional.of(variables.get(name).getValue());
        }
        if (parent != null) {
            return parent.lookup(name);
        }
        return Optional.empty();
    }
}
