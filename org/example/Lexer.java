package org.example;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private String input;
    private int pos;
    private int lineNumber;

    public List<Token> tokenizeLine(String line, int lineNumber) throws LexicalException {
        this.input = line;
        this.pos = 0;
        this.lineNumber = lineNumber;

        List<Token> tokens = new ArrayList<>();

        while (!isAtEnd()) {
            char c = currentChar();

            if (Character.isWhitespace(c)) {
                pos++;
                continue;
            }

            switch (c) {
                case '[':
                    tokens.add(new Token(TokenType.L_CORCHETE, "[", lineNumber, pos + 1));
                    pos++;
                    break;
                case ']':
                    tokens.add(new Token(TokenType.R_CORCHETE, "]", lineNumber, pos + 1));
                    pos++;
                    break;
                case '{':
                    tokens.add(new Token(TokenType.L_LLAVE, "{", lineNumber, pos + 1));
                    pos++;
                    break;
                case '}':
                    tokens.add(new Token(TokenType.R_LLAVE, "}", lineNumber, pos + 1));
                    pos++;
                    break;
                case ',':
                    tokens.add(new Token(TokenType.COMA, ",", lineNumber, pos + 1));
                    pos++;
                    break;
                case ':':
                    tokens.add(new Token(TokenType.DOS_PUNTOS, ":", lineNumber, pos + 1));
                    pos++;
                    break;
                case '"':
                    tokens.add(readString());
                    break;
                default:
                    if (Character.isDigit(c)) {
                        tokens.add(readNumber());
                    } else if (Character.isLetter(c)) {
                        tokens.add(readKeyword());
                    } else {
                        throw new LexicalException(
                                "Error lexico en linea " + lineNumber + ", columna " + (pos + 1) +
                                        ": simbolo no valido '" + c + "'"
                        );
                    }
            }
        }

        return tokens;
    }

    private Token readString() throws LexicalException {
        int start = pos;
        pos++; // skip opening quote

        while (!isAtEnd() && currentChar() != '"') {
            pos++;
        }

        if (isAtEnd()) {
            throw new LexicalException(
                    "Error lexico en linea " + lineNumber + ", columna " + (start + 1) +
                            ": cadena sin cerrar"
            );
        }

        pos++; // include closing quote
        String lexeme = input.substring(start, pos);
        return new Token(TokenType.STRING, lexeme, lineNumber, start + 1);
    }

    private Token readNumber() throws LexicalException {
        int start = pos;

        while (!isAtEnd() && Character.isDigit(currentChar())) {
            pos++;
        }

        if (!isAtEnd() && currentChar() == '.') {
            pos++;
            if (isAtEnd() || !Character.isDigit(currentChar())) {
                throw new LexicalException(
                        "Error lexico en linea " + lineNumber + ", columna " + (pos + 1) +
                                ": numero decimal mal formado"
                );
            }

            while (!isAtEnd() && Character.isDigit(currentChar())) {
                pos++;
            }
        }

        if (!isAtEnd() && (currentChar() == 'e' || currentChar() == 'E')) {
            pos++;

            if (!isAtEnd() && (currentChar() == '+' || currentChar() == '-')) {
                pos++;
            }

            if (isAtEnd() || !Character.isDigit(currentChar())) {
                throw new LexicalException(
                        "Error lexico en linea " + lineNumber + ", columna " + (pos + 1) +
                                ": exponente mal formado"
                );
            }

            while (!isAtEnd() && Character.isDigit(currentChar())) {
                pos++;
            }
        }

        String lexeme = input.substring(start, pos);
        return new Token(TokenType.NUMBER, lexeme, lineNumber, start + 1);
    }

    private Token readKeyword() throws LexicalException {
        int start = pos;

        while (!isAtEnd() && Character.isLetter(currentChar())) {
            pos++;
        }

        String lexeme = input.substring(start, pos);

        switch (lexeme) {
            case "true":
            case "TRUE":
                return new Token(TokenType.PR_TRUE, lexeme, lineNumber, start + 1);
            case "false":
            case "FALSE":
                return new Token(TokenType.PR_FALSE, lexeme, lineNumber, start + 1);
            case "null":
            case "NULL":
                return new Token(TokenType.PR_NULL, lexeme, lineNumber, start + 1);
            default:
                throw new LexicalException(
                        "Error lexico en linea " + lineNumber + ", columna " + (start + 1) +
                                ": palabra no valida '" + lexeme + "'"
                );
        }
    }

    private char currentChar() {
        return input.charAt(pos);
    }

    private boolean isAtEnd() {
        return pos >= input.length();
    }
}