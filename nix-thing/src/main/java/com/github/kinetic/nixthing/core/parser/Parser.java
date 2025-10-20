package com.github.kinetic.nixthing.core.parser;

import com.github.kinetic.nixthing.ast.*;
import com.github.kinetic.nixthing.core.token.Token;
import com.github.kinetic.nixthing.core.token.TokenType;

import java.util.ArrayList;
import java.util.List;

public final class Parser {

    private final List<Token> tokens;
    private int position = 0;

    public Parser(final List<Token> tokens) {
        this.tokens = tokens;
    }

    public NixExpression parse() {
        return parseExpression(0);
    }

    private NixExpression parseExpression(final int precedence) {
        NixExpression left = parsePrimary();

        if(left == null) return null;

        while(position < tokens.size()) {
            if(tokens.get(position).type() == TokenType.COLON) {
                position++;

                final NixExpression body = parseExpression(0);

                return new NixFunction((NixIdentifier) left, body);
            }

            if(tokens.get(position).type() == TokenType.DOT) {
                position++;

                final NixIdentifier attr = new NixIdentifier(tokens.get(position++).value());

                left = new NixSelect(left, attr);

                continue;
            }
            if(tokens.get(position).type() == TokenType.OPERATOR) {
                final int newPrecedence = getPrecedence(tokens.get(position).value());

                if(precedence >= newPrecedence) break;

                left = parseInfix(left);
            } else {
                final NixExpression right = parsePrimary();

                if(right == null) break;

                left = new NixFunctionCall(left, right);
            }
        }

        return left;
    }

    private NixExpression parseInfix(final NixExpression left) {
        final String operator = tokens.get(position).value();
        final int precedence = getPrecedence(operator);

        position++;

        final NixExpression right = parseExpression(precedence);

        return new NixBinaryOp(left, operator, right);
    }

    private int getPrecedence(final String operator) {
        return switch(operator) {
            case "==" -> 1;
            case "+", "-" -> 2;
            case "*", "/", "%" -> 3;
            default -> 0;
        };
    }

    private NixExpression parsePrimary() {
        if(position >= tokens.size()) return null;

        final Token token = tokens.get(position++);

        if(token.type() == TokenType.INTEGER) return new NixInteger(Integer.parseInt(token.value()));

        if(token.type() == TokenType.STRING) return new NixString(token.value());

        if(token.type() == TokenType.IDENTIFIER) {
            if(token.value().equals("builtins")) return new NixBuiltins();

            return new NixIdentifier(token.value());
        }
        if(token.type() == TokenType.KEYWORD && token.value().equals("true")) {
            return new NixBoolean(true);
        }
        if(token.type() == TokenType.KEYWORD && token.value().equals("false")) {
            return new NixBoolean(false);
        }
        if(token.type() == TokenType.KEYWORD && token.value().equals("if")) {
            return parseIfExpression();
        }
        if(token.type() == TokenType.KEYWORD && token.value().equals("let")) {
            return parseLetExpression();
        }
        if(token.type() == TokenType.LBRACE) return parseSetExpression();

        if(token.type() == TokenType.LBRACK) return parseListExpression();

        if(token.type() == TokenType.LPAREN) {
            NixExpression expression = parseExpression(0);
            if(position >= tokens.size() || tokens.get(position).type() != TokenType.RPAREN) {
                throw new RuntimeException("Expected ')'");
            }

            position++;

            return new NixParen(expression);
        }

        position--;
        return null;
    }

    private NixExpression parseLetExpression() {
        final List<NixBinding> bindings = new ArrayList<>();

        while(position < tokens.size() && (tokens.get(position).type() != TokenType.KEYWORD || !tokens.get(position).value().equals("in"))) {
            final NixIdentifier name = new NixIdentifier(tokens.get(position++).value());

            if(position >= tokens.size() || tokens.get(position).type() != TokenType.EQUALS) {
                throw new RuntimeException("Expected '='");
            }

            position++;

            final NixExpression value = parseExpression(0);

            bindings.add(new NixBinding(name, value));
            if(position < tokens.size() && tokens.get(position).type() == TokenType.SEMICOLON) {
                position++;
            } else if(position >= tokens.size() || (tokens.get(position).type() != TokenType.KEYWORD || !tokens.get(position).value().equals("in"))) {
                throw new RuntimeException("Expected ';' or 'in' after binding in let expression.");
            }
        }

        if(position >= tokens.size() || !tokens.get(position).value().equals("in")) {
            throw new RuntimeException("Expected 'in'");
        }

        position++;

        final NixExpression inExpression = parseExpression(0);

        return new NixLet(bindings, inExpression);
    }

    private NixExpression parseIfExpression() {
        final NixExpression condition = parseExpression(0);

        if(position >= tokens.size() || !tokens.get(position).value().equals("then")) {
            throw new RuntimeException("Expected 'then'");
        }

        position++;
        final NixExpression thenExpression = parseExpression(0);

        if(position >= tokens.size() || !tokens.get(position).value().equals("else")) {
            throw new RuntimeException("Expected 'else'");
        }

        position++;

        final NixExpression elseExpression = parseExpression(0);

        return new NixIf(condition, thenExpression, elseExpression);
    }

    private NixExpression parseListExpression() {
        final List<NixExpression> elements = new ArrayList<>();

        while(position < tokens.size() && tokens.get(position).type() != TokenType.RBRACK) {
            elements.add(parsePrimary());
        }
        if(position >= tokens.size() || tokens.get(position).type() != TokenType.RBRACK) {
            throw new RuntimeException("Expected ']'");
        }

        position++;

        return new NixList(elements);
    }

    private NixExpression parseSetExpression() {
        final List<NixBinding> bindings = new ArrayList<>();
        while(position < tokens.size() && tokens.get(position).type() != TokenType.RBRACE) {
            if(tokens.get(position).type() == TokenType.KEYWORD && tokens.get(position).value().equals("inherit")) {
                position++;

                while(position < tokens.size() && tokens.get(position).type() == TokenType.IDENTIFIER) {
                    final Token idToken = tokens.get(position++);
                    final NixIdentifier identifier = new NixIdentifier(idToken.value());

                    bindings.add(new NixBinding(identifier, new NixInherit(identifier)));
                }
            } else {
                final NixIdentifier name = new NixIdentifier(tokens.get(position++).value());

                if(position >= tokens.size() || tokens.get(position).type() != TokenType.EQUALS)
                    throw new RuntimeException("Expected '=' after identifier '" + name.getName() + "' in set.");

                position++;

                final NixExpression value = parseExpression(0);

                bindings.add(new NixBinding(name, value));
            }

            if(position < tokens.size() && tokens.get(position).type() == TokenType.SEMICOLON)
                position++;
        }

        if(position >= tokens.size() || tokens.get(position).type() != TokenType.RBRACE)
            throw new RuntimeException("Expected '}' to close set.");

        position++;

        return new NixSet(bindings);
    }
}
