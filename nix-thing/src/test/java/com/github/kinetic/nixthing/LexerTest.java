package com.github.kinetic.nixthing;

import com.github.kinetic.nixthing.core.lexer.Lexer;
import com.github.kinetic.nixthing.core.token.Token;
import com.github.kinetic.nixthing.core.token.TokenType;
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
        assertEquals(TokenType.INTEGER, tokens.get(0).type());
        assertEquals("1", tokens.get(0).value());
        assertEquals(TokenType.OPERATOR, tokens.get(1).type());
        assertEquals("+", tokens.get(1).value());
        assertEquals(TokenType.INTEGER, tokens.get(2).type());
        assertEquals("2", tokens.get(2).value());
    }

    @Test
    void testTokenizeString() {
        String input = "\"hello\"";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        assertEquals(1, tokens.size());
        assertEquals(TokenType.STRING, tokens.getFirst().type());
        assertEquals("hello", tokens.getFirst().value());
    }

    @Test
    void testTokenizeIdentifier() {
        String input = "my_var";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        assertEquals(1, tokens.size());
        assertEquals(TokenType.IDENTIFIER, tokens.getFirst().type());
        assertEquals("my_var", tokens.getFirst().value());
    }

    @Test
    void testTokenizeMixed() {
        String input = "let x = 10; in x + 1";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        assertEquals(9, tokens.size());
        assertEquals(TokenType.KEYWORD, tokens.get(0).type());
        assertEquals("let", tokens.get(0).value());
        assertEquals(TokenType.IDENTIFIER, tokens.get(1).type());
        assertEquals("x", tokens.get(1).value());
        assertEquals(TokenType.EQUALS, tokens.get(2).type());
        assertEquals("=", tokens.get(2).value());
        assertEquals(TokenType.INTEGER, tokens.get(3).type());
        assertEquals("10", tokens.get(3).value());
        assertEquals(TokenType.SEMICOLON, tokens.get(4).type());
        assertEquals(";", tokens.get(4).value());
        assertEquals(TokenType.KEYWORD, tokens.get(5).type());
        assertEquals("in", tokens.get(5).value());
        assertEquals(TokenType.IDENTIFIER, tokens.get(6).type());
        assertEquals("x", tokens.get(6).value());
        assertEquals(TokenType.OPERATOR, tokens.get(7).type());
        assertEquals("+", tokens.get(7).value());
        assertEquals(TokenType.INTEGER, tokens.get(8).type());
        assertEquals("1", tokens.get(8).value());
    }

    @Test
    void testTokenizeKeywords() {
        String input = "let in";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        assertEquals(2, tokens.size());
        assertEquals(TokenType.KEYWORD, tokens.get(0).type());
        assertEquals("let", tokens.get(0).value());
        assertEquals(TokenType.KEYWORD, tokens.get(1).type());
        assertEquals("in", tokens.get(1).value());
    }

    @Test
    void testTokenizeMultiLineString() {
        String input = "''hello\nworld''";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        assertEquals(1, tokens.size());
        assertEquals(TokenType.STRING, tokens.getFirst().type());
        assertEquals("hello\nworld", tokens.getFirst().value());
    }

    @Test
    void testTokenizeMultiLineStringWithEscapedQuotes() {
        String input = "''this is '''' a test''";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        assertEquals(1, tokens.size());
        assertEquals(TokenType.STRING, tokens.getFirst().type());
        assertEquals("this is '' a test", tokens.getFirst().value());
    }

    @Test
    void testTokenizeEmptyMultiLineString() {
        String input = "''''";
        Lexer lexer = new Lexer(input);
        List<Token> tokens = lexer.tokenize();
        assertEquals(1, tokens.size());
        assertEquals(TokenType.STRING, tokens.getFirst().type());
        assertEquals("", tokens.getFirst().value());
    }
}