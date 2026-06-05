package org.example;

import java.util.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    private final List<String> errors = new ArrayList<>();

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void parse() {
        json();

        if (!isAtEnd()) {
            error(peek(), "Se esperaba fin de archivo");
        }

        if (errors.isEmpty()) {
            System.out.println("Fuente sintacticamente correcto.");
        } else {
            System.out.println("Fuente sintacticamente incorrecto.");
            errors.forEach(System.out::println);
        }
    }

    private void json() {
        element();
        consume(TokenType.EOF, "Se esperaba EOF al final del archivo");
    }

    private void element() {
        switch (peek().getType()) {
            case L_LLAVE:
                object();
                break;
            case L_CORCHETE:
                array();
                break;
            case STRING:
            case NUMBER:
            case PR_TRUE:
            case PR_FALSE:
            case PR_NULL:
                advance();
                break;
            default:
                error(peek(), "Se esperaba un elemento JSON");
                synchronize();
        }
    }

    private void object() {
        consume(TokenType.L_LLAVE, "Se esperaba '{'");

        if (check(TokenType.R_LLAVE)) {
            advance();
            return;
        }

        attributes();

        consume(TokenType.R_LLAVE, "Se esperaba '}'");
    }

    private void attributes() {
        pair();

        while (match(TokenType.COMA)) {
            if (check(TokenType.R_LLAVE)) {
                error(peek(), "No debe haber coma antes de '}'");
                return;
            }
            pair();
        }
    }

    private void pair() {
        consume(TokenType.STRING, "Se esperaba una clave STRING");
        consume(TokenType.DOS_PUNTOS, "Se esperaba ':' despues de la clave");
        element();
    }

    private void array() {
        consume(TokenType.L_CORCHETE, "Se esperaba '['");

        if (check(TokenType.R_CORCHETE)) {
            advance();
            return;
        }

        elements();

        consume(TokenType.R_CORCHETE, "Se esperaba ']'");
    }

    private void elements() {
        element();

        while (match(TokenType.COMA)) {
            if (check(TokenType.R_CORCHETE)) {
                error(peek(), "No debe haber coma antes de ']'");
                return;
            }
            element();
        }
    }

    private boolean consume(TokenType type, String message) {
        if (check(type)) {
            advance();
            return true;
        }

        error(peek(), message);
        synchronize();
        return false;
    }

    private void synchronize() {
        while (!isAtEnd()) {
            if (previous().getType() == TokenType.COMA) return;

            switch (peek().getType()) {
                case R_LLAVE:
                case R_CORCHETE:
                case STRING:
                case L_LLAVE:
                case L_CORCHETE:
                case PR_TRUE:
                case PR_FALSE:
                case PR_NULL:
                case NUMBER:
                    return;
            }

            advance();
        }
    }

    private boolean match(TokenType type) {
        if (check(type)) {
            advance();
            return true;
        }
        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return type == TokenType.EOF;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private void error(Token token, String message) {
        errors.add("Error sintactico en linea " + token.getLine() +
                ", columna " + token.getColumn() +
                ": " + message +
                ". Se encontro: '" + token.getLexeme() + "'");
    }
}