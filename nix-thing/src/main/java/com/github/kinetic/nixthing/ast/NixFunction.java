package com.github.kinetic.nixthing.ast;

public final class NixFunction extends NixExpression {
    private final NixIdentifier argument;
    private final NixExpression body;

    public NixFunction(final NixIdentifier argument, final NixExpression body) {
        this.argument = argument;
        this.body = body;
    }

    public NixIdentifier getArgument() {
        return argument;
    }

    public NixExpression getBody() {
        return body;
    }
}
