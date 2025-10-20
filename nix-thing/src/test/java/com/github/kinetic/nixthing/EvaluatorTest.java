package com.github.kinetic.nixthing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EvaluatorTest {

    private Evaluator evaluator;
    private Environment globalEnv;

    @BeforeEach
    void setUp() {
        evaluator = new Evaluator();
        globalEnv = new Environment(null);
    }

    private NixExpression parseAndEval(String input) {
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer.tokenize());
        NixExpression expr = parser.parse();
        return evaluator.eval(expr, globalEnv);
    }

    @Test
    void testEvalInteger() {
        NixExpression result = parseAndEval("123");
        assertInstanceOf(NixInteger.class, result);
        assertEquals(123, ((NixInteger) result).getValue());
    }

    @Test
    void testEvalArithmetic() {
        NixExpression result = parseAndEval("10 + 2 * 5");
        assertInstanceOf(NixInteger.class, result);
        assertEquals(20, ((NixInteger) result).getValue());
    }

    @Test
    void testEvalLetBinding() {
        NixExpression result = parseAndEval("let x = 10; in x * 2");
        assertInstanceOf(NixInteger.class, result);
        assertEquals(20, ((NixInteger) result).getValue());
    }

    @Test
    void testEvalSetAccess() {
        NixExpression result = parseAndEval("{ a = 10; b = 20; }.a");
        assertInstanceOf(NixInteger.class, result);
        assertEquals(10, ((NixInteger) result).getValue());
    }

    @Test
    void testEvalFunctionCall() {
        NixExpression result = parseAndEval("(x: x * x) 5");
        assertInstanceOf(NixInteger.class, result);
        assertEquals(25, ((NixInteger) result).getValue());
    }

    @Test
    void testEvalIfThenElse() {
        NixExpression result = parseAndEval("if 1 == 1 then \"yes\" else \"no\"");
        assertInstanceOf(NixString.class, result);
        assertEquals("yes", ((NixString) result).getValue());
    }
}
