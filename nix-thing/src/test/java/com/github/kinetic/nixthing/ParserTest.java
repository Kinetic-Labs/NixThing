package com.github.kinetic.nixthing;

import com.github.kinetic.nixthing.ast.*;
import com.github.kinetic.nixthing.core.lexer.Lexer;
import com.github.kinetic.nixthing.core.parser.Parser;
import com.github.kinetic.nixthing.core.token.Token;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
    @Test
    void testParseInteger() {
        String input = "123";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertEquals(NixInteger.class, expression.getClass());
        assertEquals(123, ((NixInteger) expression).getValue());
    }

    @Test
    void testParseAddition() {
        String input = "1 + 2";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixBinaryOp.class, expression);
        NixBinaryOp binaryOp = (NixBinaryOp) expression;
        assertEquals("+", binaryOp.getOperator());
        assertInstanceOf(NixInteger.class, binaryOp.getLeft());
        assertEquals(1, ((NixInteger) binaryOp.getLeft()).getValue());
        assertInstanceOf(NixInteger.class, binaryOp.getRight());
        assertEquals(2, ((NixInteger) binaryOp.getRight()).getValue());
    }

    @Test
    void testParseSubtraction() {
        String input = "10 - 4";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixBinaryOp.class, expression);
        NixBinaryOp binaryOp = (NixBinaryOp) expression;
        assertEquals("-", binaryOp.getOperator());
        assertInstanceOf(NixInteger.class, binaryOp.getLeft());
        assertEquals(10, ((NixInteger) binaryOp.getLeft()).getValue());
        assertInstanceOf(NixInteger.class, binaryOp.getRight());
        assertEquals(4, ((NixInteger) binaryOp.getRight()).getValue());
    }

    @Test
    void testParseMultiplication() {
        String input = "3 * 5";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixBinaryOp.class, expression);
        NixBinaryOp binaryOp = (NixBinaryOp) expression;
        assertEquals("*", binaryOp.getOperator());
        assertInstanceOf(NixInteger.class, binaryOp.getLeft());
        assertEquals(3, ((NixInteger) binaryOp.getLeft()).getValue());
        assertInstanceOf(NixInteger.class, binaryOp.getRight());
        assertEquals(5, ((NixInteger) binaryOp.getRight()).getValue());
    }

    @Test
    void testParseDivision() {
        String input = "15 / 3";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixBinaryOp.class, expression);
        NixBinaryOp binaryOp = (NixBinaryOp) expression;
        assertEquals("/", binaryOp.getOperator());
        assertInstanceOf(NixInteger.class, binaryOp.getLeft());
        assertEquals(15, ((NixInteger) binaryOp.getLeft()).getValue());
        assertInstanceOf(NixInteger.class, binaryOp.getRight());
        assertEquals(3, ((NixInteger) binaryOp.getRight()).getValue());
    }

    @Test
    void testOperatorPrecedence() {
        String input = "1 + 2 * 3";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixBinaryOp.class, expression);
        NixBinaryOp outerOp = (NixBinaryOp) expression;
        assertEquals("+", outerOp.getOperator());

        assertInstanceOf(NixInteger.class, outerOp.getLeft());
        assertEquals(1, ((NixInteger) outerOp.getLeft()).getValue());

        assertInstanceOf(NixBinaryOp.class, outerOp.getRight());
        NixBinaryOp innerOp = (NixBinaryOp) outerOp.getRight();
        assertEquals("*", innerOp.getOperator());

        assertInstanceOf(NixInteger.class, innerOp.getLeft());
        assertEquals(2, ((NixInteger) innerOp.getLeft()).getValue());

        assertInstanceOf(NixInteger.class, innerOp.getRight());
        assertEquals(3, ((NixInteger) innerOp.getRight()).getValue());
    }

    @Test
    void testParseParentheses() {
        String input = "(1 + 2) * 3";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixBinaryOp.class, expression);
        NixBinaryOp outerOp = (NixBinaryOp) expression;
        assertEquals("*", outerOp.getOperator());

        assertInstanceOf(NixParen.class, outerOp.getLeft());
        NixParen paren = (NixParen) outerOp.getLeft();
        NixBinaryOp innerOp = (NixBinaryOp) paren.getExpression();
        assertEquals("+", innerOp.getOperator());

        assertInstanceOf(NixInteger.class, innerOp.getLeft());
        assertEquals(1, ((NixInteger) innerOp.getLeft()).getValue());

        assertInstanceOf(NixInteger.class, innerOp.getRight());
        assertEquals(2, ((NixInteger) innerOp.getRight()).getValue());

        assertInstanceOf(NixInteger.class, outerOp.getRight());
        assertEquals(3, ((NixInteger) outerOp.getRight()).getValue());
    }

    @Test
    void testParseLetExpression() {
        String input = "let x = 1; in x + 2";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixLet.class, expression);
        NixLet letExpr = (NixLet) expression;
        assertEquals(1, letExpr.getBindings().size());
        NixBinding binding = letExpr.getBindings().getFirst();
        assertEquals("x", binding.name().getName());
        assertInstanceOf(NixInteger.class, binding.value());
        assertEquals(1, ((NixInteger) binding.value()).getValue());

        assertInstanceOf(NixBinaryOp.class, letExpr.getInExpression());
        NixBinaryOp inExpr = (NixBinaryOp) letExpr.getInExpression();
        assertEquals("+", inExpr.getOperator());
        assertInstanceOf(NixIdentifier.class, inExpr.getLeft());
        assertEquals("x", ((NixIdentifier) inExpr.getLeft()).getName());
        assertInstanceOf(NixInteger.class, inExpr.getRight());
        assertEquals(2, ((NixInteger) inExpr.getRight()).getValue());
    }

    @Test
    void testParseFunctionDefinition() {
        String input = "x: x + 1";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixFunction.class, expression);
        NixFunction func = (NixFunction) expression;
        assertEquals("x", func.getArgument().getName());

        assertInstanceOf(NixBinaryOp.class, func.getBody());
        NixBinaryOp body = (NixBinaryOp) func.getBody();
        assertEquals("+", body.getOperator());
        assertInstanceOf(NixIdentifier.class, body.getLeft());
        assertEquals("x", ((NixIdentifier) body.getLeft()).getName());
        assertInstanceOf(NixInteger.class, body.getRight());
        assertEquals(1, ((NixInteger) body.getRight()).getValue());
    }

    @Test
    void testParseFunctionCall() {
        String input = "f 1";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixFunctionCall.class, expression);
        NixFunctionCall call = (NixFunctionCall) expression;
        assertInstanceOf(NixIdentifier.class, call.getFunction());
        assertEquals("f", ((NixIdentifier) call.getFunction()).getName());
        assertInstanceOf(NixInteger.class, call.getArgument());
        assertEquals(1, ((NixInteger) call.getArgument()).getValue());
    }

    @Test
    void testParseChainedFunctionCall() {
        String input = "f 1 2";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixFunctionCall.class, expression);
        NixFunctionCall outerCall = (NixFunctionCall) expression;
        assertInstanceOf(NixInteger.class, outerCall.getArgument());
        assertEquals(2, ((NixInteger) outerCall.getArgument()).getValue());

        assertInstanceOf(NixFunctionCall.class, outerCall.getFunction());
        NixFunctionCall innerCall = (NixFunctionCall) outerCall.getFunction();
        assertInstanceOf(NixIdentifier.class, innerCall.getFunction());
        assertEquals("f", ((NixIdentifier) innerCall.getFunction()).getName());
        assertInstanceOf(NixInteger.class, innerCall.getArgument());
        assertEquals(1, ((NixInteger) innerCall.getArgument()).getValue());
    }

    @Test
    void testParseBooleanTrue() {
        String input = "true";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixBoolean.class, expression);
        assertTrue(((NixBoolean) expression).getValue());
    }

    @Test
    void testParseBooleanFalse() {
        String input = "false";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixBoolean.class, expression);
        assertFalse(((NixBoolean) expression).getValue());
    }

    @Test
    void testParseIfExpression() {
        String input = "if true then 1 else 2";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixIf.class, expression);
        NixIf ifExpr = (NixIf) expression;

        assertInstanceOf(NixBoolean.class, ifExpr.getCondition());
        assertTrue(((NixBoolean) ifExpr.getCondition()).getValue());

        assertInstanceOf(NixInteger.class, ifExpr.getThenExpression());
        assertEquals(1, ((NixInteger) ifExpr.getThenExpression()).getValue());

        assertInstanceOf(NixInteger.class, ifExpr.getElseExpression());
        assertEquals(2, ((NixInteger) ifExpr.getElseExpression()).getValue());
    }

    @Test
    void testParseListExpression() {
        String input = "[1 2 3]";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixList.class, expression);
        NixList list = (NixList) expression;
        assertEquals(3, list.getElements().size());
        assertInstanceOf(NixInteger.class, list.getElements().get(0));
        assertEquals(1, ((NixInteger) list.getElements().get(0)).getValue());
        assertInstanceOf(NixInteger.class, list.getElements().get(1));
        assertEquals(2, ((NixInteger) list.getElements().get(1)).getValue());
        assertInstanceOf(NixInteger.class, list.getElements().get(2));
        assertEquals(3, ((NixInteger) list.getElements().get(2)).getValue());
    }

    @Test
    void testParseSetExpression() {
        String input = "{ a = 1; b = 2; }";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixSet.class, expression);
        NixSet set = (NixSet) expression;
        assertEquals(2, set.getBindings().size());

        NixBinding binding1 = set.getBindings().getFirst();
        assertEquals("a", binding1.name().getName());
        assertInstanceOf(NixInteger.class, binding1.value());
        assertEquals(1, ((NixInteger) binding1.value()).getValue());

        NixBinding binding2 = set.getBindings().get(1);
        assertEquals("b", binding2.name().getName());
        assertInstanceOf(NixInteger.class, binding2.value());
        assertEquals(2, ((NixInteger) binding2.value()).getValue());
    }

    @Test
    void testParseMixedSetExpression() {
        String input = "{ inherit a; b = 2; }";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixSet.class, expression);
        NixSet set = (NixSet) expression;
        assertEquals(2, set.getBindings().size());

        NixBinding binding1 = set.getBindings().getFirst();
        assertEquals("a", binding1.name().getName());
        assertInstanceOf(NixInherit.class, binding1.value());

        NixBinding binding2 = set.getBindings().get(1);
        assertEquals("b", binding2.name().getName());
        assertInstanceOf(NixInteger.class, binding2.value());
        assertEquals(2, ((NixInteger) binding2.value()).getValue());
    }

    @Test
    void testParseMultiLineString() {
        String input = "''hello\nworld''";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        NixExpression expression = parser.parse();

        assertInstanceOf(NixString.class, expression);
        assertEquals("hello\nworld", ((NixString) expression).getValue());
    }
}
