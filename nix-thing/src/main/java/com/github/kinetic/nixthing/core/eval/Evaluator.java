package com.github.kinetic.nixthing.core.eval;

import com.github.kinetic.nixthing.ast.*;
import com.github.kinetic.nixthing.core.enviornment.Environment;
import com.github.kinetic.nixthing.lang.NixClosure;

import java.util.ArrayList;
import java.util.List;

public class Evaluator {

    public NixExpression eval(final NixExpression expression, final Environment environment) {
        if(expression instanceof NixInteger ||
                expression instanceof NixString ||
                expression instanceof NixBoolean ||
                expression instanceof NixClosure) {
            return expression;
        }

        if(expression instanceof NixIdentifier)
            return environment.lookup(((NixIdentifier) expression).getName())
                    .orElseThrow(() -> new RuntimeException("Undefined variable: " + ((NixIdentifier) expression).getName()));

        if(expression instanceof NixParen)
            return eval(((NixParen) expression).getExpression(), environment);

        if(expression instanceof NixList) {
            final List<NixExpression> evaluatedElements = new ArrayList<>();

            for(final NixExpression element : ((NixList) expression).getElements())
                evaluatedElements.add(eval(element, environment));

            return new NixList(evaluatedElements);
        }

        if(expression instanceof NixSet) {
            final Environment setEnvironment = new Environment(environment);
            final List<NixBinding> originalBindings = ((NixSet) expression).getBindings();

            for(final NixBinding binding : originalBindings)
                setEnvironment.define(binding.name().getName(), binding.value(), setEnvironment);

            return new NixSet(originalBindings, setEnvironment);
        }

        if(expression instanceof NixBinaryOp)
            return evalBinaryOp((NixBinaryOp) expression, environment);

        if(expression instanceof NixInherit) {
            final String name = ((NixInherit) expression).getIdentifier().getName();

            if(environment.getParent() != null)
                return environment.getParent().lookup(name)
                        .orElseThrow(() -> new RuntimeException("Inherited variable '" + name + "' not found in scope."));

            throw new RuntimeException("Cannot inherit '" + name + "' from top-level scope.");
        }

        if(expression instanceof NixLet) {
            final Environment letEnvironment = new Environment(environment);

            for(final NixBinding binding : ((NixLet) expression).getBindings())
                letEnvironment.define(binding.name().getName(), binding.value(), letEnvironment);

            return eval(((NixLet) expression).getInExpression(), letEnvironment);
        }

        if(expression instanceof NixIf) {
            final NixExpression condition = eval(((NixIf) expression).getCondition(), environment);

            if(condition instanceof NixBoolean && ((NixBoolean) condition).getValue()) {
                return eval(((NixIf) expression).getThenExpression(), environment);
            } else {
                return eval(((NixIf) expression).getElseExpression(), environment);
            }
        }

        if(expression instanceof NixSelect) {
            final NixExpression setExpr = eval(((NixSelect) expression).getSet(), environment);

            if(!(setExpr instanceof NixSet set))
                throw new RuntimeException("Attribute selection on non-set type.");

            final String attrName = ((NixSelect) expression).getAttr().getName();

            assert set.getEnv() != null;

            return set.getEnv().lookup(attrName)
                    .orElseThrow(() -> new RuntimeException("Attribute '" + attrName + "' not found in set."));
        }

        if(expression instanceof NixFunction)
            return new NixClosure((NixFunction) expression, environment);

        if(expression instanceof NixFunctionCall) {
            final NixExpression funcExpr = eval(((NixFunctionCall) expression).getFunction(), environment);

            if(!(funcExpr instanceof NixClosure closure))
                throw new RuntimeException("Attempted to call a non-function: " + funcExpr);

            final NixExpression arg = eval(((NixFunctionCall) expression).getArgument(), environment);
            final Environment funcEnv = new Environment(closure.getEnvironment());

            funcEnv.defineEvaluated(closure.getFunction().getArgument().getName(), arg);

            return eval(closure.getFunction().getBody(), funcEnv);
        }

        throw new RuntimeException("Unknown expression type to evaluate: " + expression.getClass().getName());
    }

    private NixExpression evalBinaryOp(final NixBinaryOp binaryOp, final Environment environment) {
        final NixExpression left = eval(binaryOp.getLeft(), environment);
        final NixExpression right = eval(binaryOp.getRight(), environment);

        if(left instanceof NixInteger && right instanceof NixInteger) {
            final int leftVal = ((NixInteger) left).getValue();
            final int rightVal = ((NixInteger) right).getValue();

            switch(binaryOp.getOperator()) {
                case "+":
                    return new NixInteger(leftVal + rightVal);
                case "-":
                    return new NixInteger(leftVal - rightVal);
                case "*":
                    return new NixInteger(leftVal * rightVal);
                case "/":
                    return new NixInteger(leftVal / rightVal);
                case "%":
                    return new NixInteger(leftVal % rightVal);
                case "==":
                    return new NixBoolean(leftVal == rightVal);
            }
        }

        if(left instanceof NixString && right instanceof NixString) {
            if("+".equals(binaryOp.getOperator()))
                return new NixString(((NixString) left).getValue() + ((NixString) right).getValue());
        }

        throw new RuntimeException("Cannot apply operator '" + binaryOp.getOperator() + "' to " + left + " and " + right);
    }
}