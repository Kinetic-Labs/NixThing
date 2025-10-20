package com.github.kinetic.nixthing.ast;

import java.util.List;

public class NixList extends NixExpression {
    private final List<NixExpression> elements;

    public NixList(List<NixExpression> elements) {
        this.elements = elements;
    }

    public List<NixExpression> getElements() {
        return elements;
    }
}
