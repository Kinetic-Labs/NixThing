package com.github.kinetic.nixthing.core;

import com.github.kinetic.nixthing.ast.*;
import com.github.kinetic.nixthing.core.Token;
import com.github.kinetic.nixthing.core.TokenType;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int position = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public NixExpression parse() {
        return parseExpression(0);
    }

    private NixExpression parseExpression(int precedence) {
        NixExpression left = parsePrimary();
        if (left == null) {
            return null;
        }
        while (position < tokens.size()) {
            if (tokens.get(position).getType() == TokenType.COLON) {
                position++;
                NixExpression body = parseExpression(0);
                return new NixFunction((NixIdentifier) left, body);
            }
            if (tokens.get(position).getType() == TokenType.DOT) {
                position++;
                NixIdentifier attr = new NixIdentifier(tokens.get(position++).getValue());
                left = new NixSelect(left, attr);
                continue;
            }
            if (tokens.get(position).getType() == TokenType.OPERATOR) {
                int newPrecedence = getPrecedence(tokens.get(position).getValue());
                if (precedence >= newPrecedence) {
                    break;
                }
                left = parseInfix(left);
            } else {
                NixExpression right = parsePrimary();
                if (right == null) {
                    break;
                }
                left = new NixFunctionCall(left, right);
            }
        }
        return left;
    }

    private NixExpression parseInfix(NixExpression left) {
        String operator = tokens.get(position).getValue();
        int precedence = getPrecedence(operator);
        position++;
        NixExpression right = parseExpression(precedence);
        return new NixBinaryOp(left, operator, right);
    }

    private int getPrecedence(String operator) {
        switch (operator) {
            case "==":
                return 1;
            case "+":
            case "-":
                return 2;
            case "*":
            case "/":
            case "%":
                return 3;
            default:
                return 0;
        }
    }

    private NixExpression parsePrimary() {
        if (position >= tokens.size()) {
            return null;
        }
        Token token = tokens.get(position++);
        if (token.getType() == TokenType.INTEGER) {
            return new NixInteger(Integer.parseInt(token.getValue()));
        }
        if (token.getType() == TokenType.STRING) {
            return new NixString(token.getValue());
        }
        if (token.getType() == TokenType.IDENTIFIER) {
            if (token.getValue().equals("builtins")) {
                return new NixBuiltins();
            }
            return new NixIdentifier(token.getValue());
        }
        if (token.getType() == TokenType.KEYWORD && token.getValue().equals("true")) {
            return new NixBoolean(true);
        }
        if (token.getType() == TokenType.KEYWORD && token.getValue().equals("false")) {
            return new NixBoolean(false);
        }
        if (token.getType() == TokenType.KEYWORD && token.getValue().equals("if")) {
            return parseIfExpression();
        }
        if (token.getType() == TokenType.KEYWORD && token.getValue().equals("let")) {
            return parseLetExpression();
        }
        if (token.getType() == TokenType.LBRACE) {
            return parseSetExpression();
        }
        if (token.getType() == TokenType.LBRACK) {
            return parseListExpression();
        }
        if (token.getType() == TokenType.LPAREN) {
            NixExpression expression = parseExpression(0);
            if (position >= tokens.size() || tokens.get(position).getType() != TokenType.RPAREN) {
                throw new RuntimeException("Expected ')'");
            }
            position++;
            return new NixParen(expression);
        }
        position--;
        return null;
    }

    private NixExpression parseLetExpression() {
        List<NixBinding> bindings = new ArrayList<>();
        while (position < tokens.size() && (tokens.get(position).getType() != TokenType.KEYWORD || !tokens.get(position).getValue().equals("in"))) {
            NixIdentifier name = new NixIdentifier(tokens.get(position++).getValue());
            if (position >= tokens.size() || tokens.get(position).getType() != TokenType.EQUALS) {
                throw new RuntimeException("Expected '='");
            }
            position++;
            NixExpression value = parseExpression(0);
            bindings.add(new NixBinding(name, value));
            if (position < tokens.size() && tokens.get(position).getType() == TokenType.SEMICOLON) {
                position++;
            } else if (position >= tokens.size() || (tokens.get(position).getType() != TokenType.KEYWORD || !tokens.get(position).getValue().equals("in"))) {
                throw new RuntimeException("Expected ';' or 'in' after binding in let expression.");
            }
        }
        if (position >= tokens.size() || !tokens.get(position).getValue().equals("in")) {
            throw new RuntimeException("Expected 'in'");
        }
        position++;
        NixExpression inExpression = parseExpression(0);
        return new NixLet(bindings, inExpression);
    }

    private NixExpression parseIfExpression() {
        NixExpression condition = parseExpression(0);
        if (position >= tokens.size() || !tokens.get(position).getValue().equals("then")) {
            throw new RuntimeException("Expected 'then'");
        }
        position++;
        NixExpression thenExpression = parseExpression(0);
        if (position >= tokens.size() || !tokens.get(position).getValue().equals("else")) {
            throw new RuntimeException("Expected 'else'");
        }
        position++;
        NixExpression elseExpression = parseExpression(0);
        return new NixIf(condition, thenExpression, elseExpression);
    }

    private NixExpression parseListExpression() {
        List<NixExpression> elements = new ArrayList<>();
        while (position < tokens.size() && tokens.get(position).getType() != TokenType.RBRACK) {
            elements.add(parsePrimary());
        }
        if (position >= tokens.size() || tokens.get(position).getType() != TokenType.RBRACK) {
            throw new RuntimeException("Expected ']'");
        }
        position++;
        return new NixList(elements);
    }

    private NixExpression parseSetExpression() {
        List<NixBinding> bindings = new ArrayList<>();
        while (position < tokens.size() && tokens.get(position).getType() != TokenType.RBRACE) {
            if (tokens.get(position).getType() == TokenType.KEYWORD && tokens.get(position).getValue().equals("inherit")) {
                position++;
                while (position < tokens.size() && tokens.get(position).getType() == TokenType.IDENTIFIER) {
                    Token idToken = tokens.get(position++);
                    NixIdentifier identifier = new NixIdentifier(idToken.getValue());
                    bindings.add(new NixBinding(identifier, new NixInherit(identifier)));
                }
            } else {
                NixIdentifier name = new NixIdentifier(tokens.get(position++).getValue());
                if (position >= tokens.size() || tokens.get(position).getType() != TokenType.EQUALS) {
                    throw new RuntimeException("Expected '=' after identifier '" + name.getName() + "' in set.");
                }
                position++;
                NixExpression value = parseExpression(0);
                bindings.add(new NixBinding(name, value));
            }

            if (position < tokens.size() && tokens.get(position).getType() == TokenType.SEMICOLON) {
                position++;
            }
        }

        if (position >= tokens.size() || tokens.get(position).getType() != TokenType.RBRACE) {
            throw new RuntimeException("Expected '}' to close set.");
        }
        position++;
        return new NixSet(bindings);
    }
}