package com.github.kinetic.nixthing.ast;

public final class NixImport extends NixExpression {

    private final NixExpression path;

    public NixImport(final NixExpression path) {
        this.path = path;
    }

    public NixExpression getPath() {
        return path;
    }
}
