package com.github.kinetic.nixthing.core.lexer;

import com.github.kinetic.nixthing.core.token.Token;
import com.github.kinetic.nixthing.core.token.TokenType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class Lexer {

    private static final Set<String> keywords = Set.of(
            "let",
            "in",
            "true",
            "false",
            "if",
            "then",
            "else",
            "inherit",
            "import"
    );
    private final String input;
    private int position = 0;

    public Lexer(final String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        final List<Token> tokens = new ArrayList<>();

        while(position < input.length()) {
            final char current = input.charAt(position);

            if(current == '#') {
                while(position < input.length() && input.charAt(position) != '\n')
                    position++;

                continue;
            }

            if(Character.isWhitespace(current)) {
                position++;

                continue;
            }

            if(Character.isDigit(current)) {
                tokens.add(new Token(TokenType.INTEGER, readInteger()));
            } else if(current == '"') {
                tokens.add(new Token(TokenType.STRING, readString()));
            } else if(current == '\'') {
                if(position + 1 < input.length() && input.charAt(position + 1) == '\'') {
                    tokens.add(new Token(TokenType.STRING, readMultiLineString()));
                } else {
                    throw new RuntimeException("Unexpected character: " + current);
                }
            } else if(Character.isLetter(current) || current == '_') {
                final String identifier = readIdentifier();

                if(keywords.contains(identifier)) {
                    tokens.add(new Token(TokenType.KEYWORD, identifier));
                } else {
                    tokens.add(new Token(TokenType.IDENTIFIER, identifier));
                }
            } else {
                switch(current) {
                    case '=':
                        if(position + 1 < input.length() && input.charAt(position + 1) == '=') {
                            tokens.add(new Token(TokenType.OPERATOR, "=="));
                            position += 2;
                        } else {
                            tokens.add(new Token(TokenType.EQUALS, "="));
                            position++;
                        }
                        break;
                    case '{': {
                        tokens.add(new Token(TokenType.LBRACE, "{"));
                        position++;
                        break;
                    }
                    case '}': {
                        tokens.add(new Token(TokenType.RBRACE, "}"));
                        position++;
                        break;
                    }
                    case '(': {
                        tokens.add(new Token(TokenType.LPAREN, "("));
                        position++;
                        break;
                    }
                    case ')': {
                        tokens.add(new Token(TokenType.RPAREN, ")"));
                        position++;
                        break;
                    }
                    case '[': {
                        tokens.add(new Token(TokenType.LBRACK, "["));
                        position++;
                        break;
                    }
                    case ']': {
                        tokens.add(new Token(TokenType.RBRACK, "]"));
                        position++;
                        break;
                    }
                    case ';': {
                        tokens.add(new Token(TokenType.SEMICOLON, ";"));
                        position++;
                        break;
                    }
                    case ':': {
                        tokens.add(new Token(TokenType.COLON, ":"));
                        position++;
                        break;
                    }
                    case '.': {
                        tokens.add(new Token(TokenType.DOT, "."));
                        position++;
                        break;
                    }
                    case '+':
                    case '-':
                    case '*':
                    case '/':
                    case '%': {
                        tokens.add(
                                new Token(
                                        TokenType.OPERATOR,
                                        Character.toString(current)
                                )
                        );
                        position++;
                        break;
                    }
                    default: {
                        throw new RuntimeException(
                                "Unexpected character: " + current
                        );
                    }
                }
            }
        }

        return tokens;
    }

    private String readInteger() {
        final StringBuilder builder = new StringBuilder();

        while(position < input.length() && Character.isDigit(input.charAt(position))) {
            builder.append(input.charAt(position));

            position++;
        }

        return builder.toString();
    }

    private String readString() {
        final StringBuilder builder = new StringBuilder();

        position++;

        while(position < input.length() && input.charAt(position) != '"') {
            builder.append(input.charAt(position));

            position++;
        }

        position++;

        return builder.toString();
    }

    private String readIdentifier() {
        final StringBuilder builder = new StringBuilder();
        while(position < input.length() &&
                (Character.isLetterOrDigit(input.charAt(position)) ||
                        input.charAt(position) == '_')) {
            builder.append(input.charAt(position));

            position++;
        }

        return builder.toString();
    }

    private String readMultiLineString() {
        final StringBuilder builder = new StringBuilder();

        position += 2;

        while(position < input.length()) {
            if(position + 3 < input.length() && input.charAt(position) == '\'' &&
                    input.charAt(position + 1) == '\'' &&
                    input.charAt(position + 2) == '\'' &&
                    input.charAt(position + 3) == '\''
            ) {
                builder.append("''");

                position += 4;
            } else if(position + 1 < input.length() && input.charAt(position) == '\'' && input.charAt(position + 1) == '\'') {
                position += 2;

                return builder.toString();
            } else {
                builder.append(input.charAt(position));

                position++;
            }
        }

        throw new RuntimeException("Unterminated multi-line string");
    }
}
