package com.github.kinetic.nixthing.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import com.github.kinetic.nixthing.core.Token;
import com.github.kinetic.nixthing.core.TokenType;

public class Lexer {
    private final String input;
    private int position = 0;
    private static final Set<String> keywords = Set.of("let", "in", "true", "false", "if", "then", "else", "inherit");

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (position < input.length()) {
            char current = input.charAt(position);
            if (current == '#') {
                while (position < input.length() && input.charAt(position) != '\n') {
                    position++;
                }
                continue;
            }
            if (Character.isWhitespace(current)) {
                position++;
                continue;
            }
            if (Character.isDigit(current)) {
                tokens.add(new Token(TokenType.INTEGER, readInteger()));
            } else if (current == '"') {
                tokens.add(new Token(TokenType.STRING, readString()));
            } else if (Character.isLetter(current) || current == '_') {
                String identifier = readIdentifier();
                if (keywords.contains(identifier)) {
                    tokens.add(new Token(TokenType.KEYWORD, identifier));
                } else {
                    tokens.add(new Token(TokenType.IDENTIFIER, identifier));
                }
            } else {
                switch (current) {
                    case '=':
                        if (position + 1 < input.length() && input.charAt(position + 1) == '=') {
                            tokens.add(new Token(TokenType.OPERATOR, "=="));
                            position += 2;
                        } else {
                            tokens.add(new Token(TokenType.EQUALS, "="));
                            position++;
                        }
                        break;
                    case '{':
                        tokens.add(new Token(TokenType.LBRACE, "{"));
                        position++;
                        break;
                    case '}':
                        tokens.add(new Token(TokenType.RBRACE, "}"));
                        position++;
                        break;
                    case '(': 
                        tokens.add(new Token(TokenType.LPAREN, "("));
                        position++;
                        break;
                    case ')':
                        tokens.add(new Token(TokenType.RPAREN, ")"));
                        position++;
                        break;
                    case '[':
                        tokens.add(new Token(TokenType.LBRACK, "["));
                        position++;
                        break;
                    case ']':
                        tokens.add(new Token(TokenType.RBRACK, "]"));
                        position++;
                        break;
                    case ';':
                        tokens.add(new Token(TokenType.SEMICOLON, ";"));
                        position++;
                        break;
                    case ':':
                        tokens.add(new Token(TokenType.COLON, ":"));
                        position++;
                        break;
                    case '.':
                        tokens.add(new Token(TokenType.DOT, "."));
                        position++;
                        break;
                    case '+':
                    case '-':
                    case '*':
                    case '/':
                    case '%':
                        tokens.add(new Token(TokenType.OPERATOR, Character.toString(current)));
                        position++;
                        break;
                    default:
                        throw new RuntimeException("Unexpected character: " + current);
                }
            }
        }
        return tokens;
    }

    private String readInteger() {
        StringBuilder builder = new StringBuilder();
        while (position < input.length() && Character.isDigit(input.charAt(position))) {
            builder.append(input.charAt(position));
            position++;
        }
        return builder.toString();
    }

    private String readString() {
        StringBuilder builder = new StringBuilder();
        position++; // Skip the opening "
        while (position < input.length() && input.charAt(position) != '"') {
            builder.append(input.charAt(position));
            position++;
        }
        position++; // Skip the closing "
        return builder.toString();
    }

    private String readIdentifier() {
        StringBuilder builder = new StringBuilder();
        while (position < input.length() && (Character.isLetterOrDigit(input.charAt(position)) || input.charAt(position) == '_')) {
            builder.append(input.charAt(position));
            position++;
        }
        return builder.toString();
    }
}
