package com.github.kinetic.nixthing.core.enviornment;

import com.github.kinetic.nixthing.ast.NixExpression;
import com.github.kinetic.nixthing.lang.Lazy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Environment {

    private final Environment parent;
    private final Map<String, Lazy> variables = new HashMap<>();

    public Environment(final Environment parent) {
        this.parent = parent;
    }

    public Environment getParent() {
        return parent;
    }

    public Map<String, Lazy> getVariables() {
        return variables;
    }

    /**
     * Define an environment variable
     *
     * @param name  name of variable
     * @param value value of variable
     * @param env   the environment to define in
     */
    public void define(final String name, final NixExpression value, final Environment env) {
        variables.put(name, new Lazy(value, env));
    }

    /**
     * Define an evaluated environment variable
     *
     * @param name  the name of variable
     * @param value the value of variable
     */
    public void defineEvaluated(final String name, final NixExpression value) {
        variables.put(name, Lazy.evaluated(value));
    }

    /**
     * Look for a variable in a given environment
     *
     * @param name name of variable
     * @return {@link NixExpression} if found, nothing if not
     */
    public Optional<NixExpression> lookup(final String name) {
        if(variables.containsKey(name))
            return Optional.of(variables.get(name).getValue());

        if(parent != null)
            return parent.lookup(name);

        return Optional.empty();
    }
}
