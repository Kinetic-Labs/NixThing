package com.github.kinetic.nixthing.core;

import com.github.kinetic.nixthing.ast.*;
import com.github.kinetic.nixthing.lang.NixClosure;

import java.util.ArrayList;
import java.util.List;

public class Evaluator {

    public NixExpression eval(NixExpression expression, Environment environment) {
        if (expression instanceof NixInteger || expression instanceof NixString || expression instanceof NixBoolean || expression instanceof NixClosure) {
            return expression;
        }

        if (expression instanceof NixIdentifier) {
            return environment.lookup(((NixIdentifier) expression).getName())
                .orElseThrow(() -> new RuntimeException("Undefined variable: " + ((NixIdentifier) expression).getName()));
        }

        if (expression instanceof NixParen) {
            return eval(((NixParen) expression).getExpression(), environment);
        }

        if (expression instanceof NixList) {
            List<NixExpression> evaluatedElements = new ArrayList<>();
            for (NixExpression element : ((NixList) expression).getElements()) {
                evaluatedElements.add(eval(element, environment));
            }
            return new NixList(evaluatedElements);
        }

        if (expression instanceof NixSet) {
            Environment setEnvironment = new Environment(environment);
            List<NixBinding> originalBindings = ((NixSet) expression).getBindings();
            for (NixBinding binding : originalBindings) {
                setEnvironment.define(binding.getName().getName(), binding.getValue(), setEnvironment);
            }
            return new NixSet(originalBindings, setEnvironment);
        }

        if (expression instanceof NixBinaryOp) {
            return evalBinaryOp((NixBinaryOp) expression, environment);
        }

        if (expression instanceof NixInherit) {
            String name = ((NixInherit) expression).getIdentifier().getName();
            if (environment.getParent() != null) {
                return environment.getParent().lookup(name)
                    .orElseThrow(() -> new RuntimeException("Inherited variable '" + name + "' not found in scope."));
            }
            throw new RuntimeException("Cannot inherit '" + name + "' from top-level scope.");
        }

        if (expression instanceof NixLet) {
            Environment letEnvironment = new Environment(environment);
            for (NixBinding binding : ((NixLet) expression).getBindings()) {
                letEnvironment.define(binding.getName().getName(), binding.getValue(), letEnvironment);
            }
            return eval(((NixLet) expression).getInExpression(), letEnvironment);
        }

        if (expression instanceof NixIf) {
            NixExpression condition = eval(((NixIf) expression).getCondition(), environment);
            if (condition instanceof NixBoolean && ((NixBoolean) condition).getValue()) {
                return eval(((NixIf) expression).getThenExpression(), environment);
            } else {
                return eval(((NixIf) expression).getElseExpression(), environment);
            }
        }

        if (expression instanceof NixSelect) {
            NixExpression setExpr = eval(((NixSelect) expression).getSet(), environment);
            if (!(setExpr instanceof NixSet)) {
                throw new RuntimeException("Attribute selection on non-set type.");
            }
            NixSet set = (NixSet) setExpr;
            String attrName = ((NixSelect) expression).getAttr().getName();
            return set.getEnv().lookup(attrName)
                .orElseThrow(() -> new RuntimeException("Attribute '" + attrName + "' not found in set."));
        }

        if (expression instanceof NixFunction) {
            return new NixClosure((NixFunction) expression, environment);
        }

        if (expression instanceof NixFunctionCall) {
            NixExpression funcExpr = eval(((NixFunctionCall) expression).getFunction(), environment);
            if (!(funcExpr instanceof NixClosure)) {
                throw new RuntimeException("Attempted to call a non-function: " + funcExpr);
            }
            NixClosure closure = (NixClosure) funcExpr;

            NixExpression arg = eval(((NixFunctionCall) expression).getArgument(), environment);
            Environment funcEnv = new Environment(closure.getEnvironment());
            funcEnv.defineEvaluated(closure.getFunction().getArgument().getName(), arg);
            return eval(closure.getFunction().getBody(), funcEnv);
        }

        throw new RuntimeException("Unknown expression type to evaluate: " + expression.getClass().getName());
    }

    private NixExpression evalBinaryOp(NixBinaryOp binaryOp, Environment environment) {
        NixExpression left = eval(binaryOp.getLeft(), environment);
        NixExpression right = eval(binaryOp.getRight(), environment);

        if (left instanceof NixInteger && right instanceof NixInteger) {
            int leftVal = ((NixInteger) left).getValue();
            int rightVal = ((NixInteger) right).getValue();
            switch (binaryOp.getOperator()) {
                case "+": return new NixInteger(leftVal + rightVal);
                case "-": return new NixInteger(leftVal - rightVal);
                case "*": return new NixInteger(leftVal * rightVal);
                case "/": return new NixInteger(leftVal / rightVal);
                case "%": return new NixInteger(leftVal % rightVal);
                case "==": return new NixBoolean(leftVal == rightVal);
            }
        }
        
        if (left instanceof NixString && right instanceof NixString) {
             if ("+".equals(binaryOp.getOperator())) {
                return new NixString(((NixString) left).getValue() + ((NixString) right).getValue());
            }
        }

        throw new RuntimeException("Cannot apply operator '" + binaryOp.getOperator() + "' to " + left + " and " + right);
    }
}