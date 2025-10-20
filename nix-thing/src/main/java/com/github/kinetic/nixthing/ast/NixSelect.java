package com.github.kinetic.nixthing.ast;

public class NixSelect extends NixExpression {
    private final NixExpression set;
    private final NixIdentifier attr;

    public NixSelect(NixExpression set, NixIdentifier attr) {
        this.set = set;
        this.attr = attr;
    }

    public NixExpression getSet() {
        return set;
    }

    public NixIdentifier getAttr() {
        return attr;
    }

    @Override
    public String toString() {
        return "NixSelect{" +
                "set=" + set +
                ", attr=" + attr +
                '}';
    }
}
