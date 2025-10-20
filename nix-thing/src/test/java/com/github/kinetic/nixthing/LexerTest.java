package com.github.kinetic.nixthing;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LexerTest {
    @Test
    void testTokenize() {
        String input = "1 + 2";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        assertEquals(3, tokens.size());
        assertEquals(TokenType.INTEGER, tokens.get(0).getType());
        assertEquals("1", tokens.get(0).getValue());
        assertEquals(TokenType.OPERATOR, tokens.get(1).getType());
        assertEquals("+", tokens.get(1).getValue());
        assertEquals(TokenType.INTEGER, tokens.get(2).getType());
        assertEquals("2", tokens.get(2).getValue());
    }

    @Test
    void testTokenizeString() {
        String input = "\"hello\"";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        assertEquals(1, tokens.size());
        assertEquals(TokenType.STRING, tokens.get(0).getType());
        assertEquals("hello", tokens.get(0).getValue());
    }

    @Test
    void testTokenizeIdentifier() {
        String input = "my_var";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        assertEquals(1, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.get(0).getType());
        assertEquals("my_var", tokens.get(0).getValue());
    }

    @Test
    void testTokenizeMixed() {
        String input = "let x = 10; in x + 1";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        assertEquals(9, tokens.size());
        assertEquals(TokenType.KEYWORD, tokens.get(0).getType());
        assertEquals("let", tokens.get(0).getValue());
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).getType());
        assertEquals("x", tokens.get(1).getValue());
        assertEquals(TokenType.EQUALS, tokens.get(2).getType());
        assertEquals("=", tokens.get(2).getValue());
        assertEquals(TokenType.INTEGER, tokens.get(3).getType());
        assertEquals("10", tokens.get(3).getValue());
        assertEquals(TokenType.SEMICOLON, tokens.get(4).getType());
        assertEquals(";", tokens.get(4).getValue());
        assertEquals(TokenType.KEYWORD, tokens.get(5).getType());
        assertEquals("in", tokens.get(5).getValue());
        assertEquals(TokenType.IDENTIFIER, tokens.get(6).getType());
        assertEquals("x", tokens.get(6).getValue());
        assertEquals(TokenType.OPERATOR, tokens.get(7).getType());
        assertEquals("+", tokens.get(7).getValue());
        assertEquals(TokenType.INTEGER, tokens.get(8).getType());
        assertEquals("1", tokens.get(8).getValue());
    }

    @Test
    void testTokenizeKeywords() {
        String input = "let in";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        assertEquals(2, tokens.size());
        assertEquals(TokenType.KEYWORD, tokens.get(0).getType());
        assertEquals("let", tokens.get(0).getValue());
        assertEquals(TokenType.KEYWORD, tokens.get(1).getType());
        assertEquals("in", tokens.get(1).getValue());
    }
}