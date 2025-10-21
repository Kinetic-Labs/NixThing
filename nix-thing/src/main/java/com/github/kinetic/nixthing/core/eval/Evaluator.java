package com.github.kinetic.nixthing.core.eval;

import com.github.kinetic.nixthing.ast.*;
import com.github.kinetic.nixthing.core.enviornment.Environment;
import com.github.kinetic.nixthing.core.lexer.Lexer;
import com.github.kinetic.nixthing.core.parser.Parser;
import com.github.kinetic.nixthing.exception.evaluator.*;
import com.github.kinetic.nixthing.lang.NixClosure;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Evaluator {

    private final Set<String> importedFiles = new HashSet<>();
    private Path basePath = Paths.get(".");

    /**
     * Set basepath for evaluator
     *
     * @param basePath new path to use
     */
    public void setBasePath(final Path basePath) {
        this.basePath = basePath;
    }

    /**
     * Evaluate a given expression inside a given environment
     *
     * @param expression  expression to evaluate
     * @param environment environment to evaluate inside of
     * @return the evaluated expression
     */
    public NixExpression eval(final NixExpression expression, final Environment environment) {
        if(expression instanceof NixInteger ||
                expression instanceof NixString ||
                expression instanceof NixBoolean ||
                expression instanceof NixClosure) {
            return expression;
        }

        if(expression instanceof NixIdentifier)
            return environment.lookup(((NixIdentifier) expression).getName())
                    .orElseThrow(() -> new UndefinedException(((NixIdentifier) expression).getName()));

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
                        .orElseThrow(() -> new InheritErrorException.InheritScopeErrorException(name));

            throw new InheritErrorException.InheritTopLevelScopeErrorException(name);
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

            if(closure.getFunction().getBody() == null)
                return evalBuiltin(closure, (NixInteger) arg);

            final Environment funcEnv = new Environment(closure.getEnvironment());

            funcEnv.defineEvaluated(closure.getFunction().getArgument().getName(), arg);

            return eval(closure.getFunction().getBody(), funcEnv);
        }

        if(expression instanceof NixImport)
            return evalImport((NixImport) expression, environment);

        if(expression instanceof NixBuiltins)
            return createBuiltinsSet();

        throw new UnknownExpressionException(expression.getClass().getName());
    }

    /**
     * Evaluate a builtin function
     *
     * @param closure closure of builtin
     * @param arg     the argument of builtin
     * @return the result of running builtin
     */
    private NixExpression evalBuiltin(final NixClosure closure, final NixInteger arg) {
        final String builtinName = closure.toString();

        if("<builtin:mod>".equals(builtinName)) {
            // mod is curried: mod x y -> x % y
            // first call returns closure waiting for second argument
            if(arg == null)
                throw new UnknownFunctionException("builtin", "mod", "Hint: 'mod' only accepts integer arguments!");

            final int firstArg = arg.getValue();

            final NixFunction secondFunc = new NixFunction(
                    new NixIdentifier("y"),
                    null
            );
            return new NixClosure(secondFunc, null) {
                @Override
                public String toString() {
                    return "<builtin:mod-partial:" + firstArg + ">";
                }
            };
        }

        if(builtinName.startsWith("<builtin:mod-partial:")) {
            final int firstArg = Integer.parseInt(builtinName.substring(21, builtinName.length() - 1));

            if(arg == null)
                throw new UnknownFunctionException("builtin", "mod", "Hint: 'mod' only accepts integer arguments!");

            final int secondArg = arg.getValue();

            if(secondArg == 0)
                throw new RuntimeException("Division by zero in builtins.mod");

            return new NixInteger(firstArg % secondArg);
        }

        if("<builtin:debug>".equals(builtinName)) {
            System.out.println("[nix-thing :: builtins.debug] " + arg);

            return arg;
        }

        throw new UnknownFunctionException("builtin", builtinName);
    }

    private NixSet createBuiltinsSet() {
        final Environment builtinsEnv = new Environment(null);
        final List<NixBinding> bindings = new ArrayList<>();
        final NixFunction modFunc = new NixFunction(
                new NixIdentifier("x"),
                null
        );
        final NixClosure modClosure = new NixClosure(modFunc, null) {
            @Override
            public String toString() {
                return "<builtin:mod>";
            }
        };

        bindings.add(new NixBinding(new NixIdentifier("mod"), modClosure));
        builtinsEnv.defineEvaluated("mod", modClosure);

        final NixFunction debugFunc = new NixFunction(
                new NixIdentifier("msg"),
                null
        );
        final NixClosure debugClosure = new NixClosure(debugFunc, null) {
            @Override
            public String toString() {
                return "<builtin:debug>";
            }
        };

        bindings.add(new NixBinding(new NixIdentifier("debug"), debugClosure));
        builtinsEnv.defineEvaluated("debug", debugClosure);

        return new NixSet(bindings, builtinsEnv);
    }

    /**
     * Evaluate and resolve an import
     *
     * @param importExpr  import expression
     * @param environment environment to evaluate inside
     * @return the resolved import
     */
    private NixExpression evalImport(final NixImport importExpr, final Environment environment) {
        final NixExpression pathExpr = eval(importExpr.getPath(), environment);

        if(!(pathExpr instanceof NixString))
            throw new UnresolvedImportException("Import path must be a string, got: " + pathExpr);

        final String pathStr = ((NixString) pathExpr).getValue();
        final Path resolvedPath = basePath.resolve(pathStr).normalize();

        final String canonicalPath;
        try {
            canonicalPath = resolvedPath.toRealPath().toString();
        } catch(IOException ioException) {
            throw new UnresolvedImportException("Cannot resolve import path: " + pathStr, ioException);
        }

        if(importedFiles.contains(canonicalPath))
            throw new RuntimeException("Circular import detected: " + pathStr);

        importedFiles.add(canonicalPath);

        try {
            final String content = Files.readString(resolvedPath, StandardCharsets.UTF_8);
            final Lexer lexer = new Lexer(content);
            final Parser parser = new Parser(lexer.tokenize());
            final NixExpression parsedExpr = parser.parse();

            final Path previousBasePath = this.basePath;
            this.basePath = resolvedPath.getParent();

            try {
                return eval(parsedExpr, environment);
            } finally {
                this.basePath = previousBasePath;
                importedFiles.remove(canonicalPath);
            }
        } catch(IOException ioException) {
            throw new UnresolvedImportException(pathStr, ioException);
        }
    }

    /**
     * Evaluate a binary operation
     *
     * @param binaryOp    operation to evaluate
     * @param environment environment to evaluate inside of
     * @return the evaluated expression
     */
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
                case "==":
                    return new NixBoolean(leftVal == rightVal);
            }
        }

        if(left instanceof NixString && right instanceof NixString) {
            if("+".equals(binaryOp.getOperator()))
                return new NixString(((NixString) left).getValue() + ((NixString) right).getValue());
        }

        throw new InvalidBinaryOperationException(binaryOp.getOperator(), left, right);
    }
}
