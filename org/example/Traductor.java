package org.example;

import java.util.ArrayList;
import java.util.List;

public class Traductor {

    private final List<Token> tokens;
    private int current = 0;
    private final List<String> errors = new ArrayList<>();

    public Traductor(List<Token> tokens) {
        this.tokens = tokens;
    }

    public String parseToXml() {
        String xml = rootObject();
        consume(TokenType.EOF, "Se esperaba EOF al final del archivo");
        return xml;
    }

    public List<String> getErrors() {
        return errors;
    }

    private String rootObject() {
        StringBuilder xml = new StringBuilder();

        consume(TokenType.L_LLAVE, "El JSON debe iniciar con '{'");

        if (check(TokenType.R_LLAVE)) {
            error(peek(), "El objeto raiz no puede estar vacio");
            advance();
            return "";
        }

        xml.append(attributeAsRoot());

        while (match(TokenType.COMA)) {
            error(peek(), "El objeto raiz debe tener un solo atributo principal");
            attributeAsRoot();
        }

        consume(TokenType.R_LLAVE, "Se esperaba '}' al final del objeto raiz");

        return xml.toString();
    }

    private String attributeAsRoot() {
        Token name = consumeAndReturn(TokenType.STRING, "Se esperaba el nombre del atributo raiz");

        if (name == null) {
            synchronize();
            return "";
        }

        consume(TokenType.DOS_PUNTOS, "Se esperaba ':' despues del nombre del atributo raiz");

        String tagName = cleanString(name.getLexeme());

        return value(tagName, false);
    }

    private String objectAsItem() {
        StringBuilder xml = new StringBuilder();

        consume(TokenType.L_LLAVE, "Se esperaba '{'");

        xml.append("<item>\n");

        if (!check(TokenType.R_LLAVE)) {
            xml.append(attributes());
        }

        consume(TokenType.R_LLAVE, "Se esperaba '}'");

        xml.append("</item>\n");

        return xml.toString();
    }

    private String objectContent(String tagName) {
        StringBuilder xml = new StringBuilder();

        consume(TokenType.L_LLAVE, "Se esperaba '{'");

        xml.append("<").append(tagName).append(">\n");

        if (!check(TokenType.R_LLAVE)) {
            xml.append(attributes());
        }

        consume(TokenType.R_LLAVE, "Se esperaba '}'");

        xml.append("</").append(tagName).append(">\n");

        return xml.toString();
    }

    private String attributes() {
        StringBuilder xml = new StringBuilder();

        xml.append(attribute());

        while (match(TokenType.COMA)) {
            if (check(TokenType.R_LLAVE)) {
                error(peek(), "No debe haber coma antes de '}'");
                return xml.toString();
            }

            xml.append(attribute());
        }

        return xml.toString();
    }

    private String attribute() {
        Token name = consumeAndReturn(TokenType.STRING, "Se esperaba una clave STRING");

        if (name == null) {
            synchronize();
            return "";
        }

        consume(TokenType.DOS_PUNTOS, "Se esperaba ':' despues de la clave");

        String tagName = cleanString(name.getLexeme());

        return value(tagName, false);
    }

    private String value(String tagName, boolean insideArray) {
        switch (peek().getType()) {
            case L_LLAVE:
                if (insideArray) {
                    return objectAsItem();
                }
                return objectContent(tagName);

            case L_CORCHETE:
                return array(tagName);

            case STRING:
            case NUMBER:
            case PR_TRUE:
            case PR_FALSE:
            case PR_NULL:
                Token value = advance();
                return "<" + tagName + ">" + value.getLexeme() + "</" + tagName + ">\n";

            default:
                error(peek(), "Se esperaba un valor JSON");
                synchronize();
                return "";
        }
    }

    private String array(String tagName) {
        StringBuilder xml = new StringBuilder();

        consume(TokenType.L_CORCHETE, "Se esperaba '['");

        if (check(TokenType.R_CORCHETE)) {
            advance();
            return "<" + tagName + "/>\n";
        }

        xml.append("<").append(tagName).append(">\n");

        xml.append(arrayValue());

        while (match(TokenType.COMA)) {
            if (check(TokenType.R_CORCHETE)) {
                error(peek(), "No debe haber coma antes de ']'");
                break;
            }

            xml.append(arrayValue());
        }

        consume(TokenType.R_CORCHETE, "Se esperaba ']'");

        xml.append("</").append(tagName).append(">\n");

        return xml.toString();
    }

    private String arrayValue() {
        switch (peek().getType()) {
            case L_LLAVE:
                return objectAsItem();

            case L_CORCHETE:
                return array("item");

            case STRING:
            case NUMBER:
            case PR_TRUE:
            case PR_FALSE:
            case PR_NULL:
                Token value = advance();
                return "<item>" + value.getLexeme() + "</item>\n";

            default:
                error(peek(), "Se esperaba un elemento dentro del arreglo");
                synchronize();
                return "";
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

    private Token consumeAndReturn(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }

        error(peek(), message);
        return null;
    }

    private void synchronize() {
        while (!isAtEnd()) {
            switch (peek().getType()) {
                case COMA:
                case R_LLAVE:
                case R_CORCHETE:
                case L_LLAVE:
                case L_CORCHETE:
                case STRING:
                case NUMBER:
                case PR_TRUE:
                case PR_FALSE:
                case PR_NULL:
                    return;
                default:
                    advance();
            }
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
        if (isAtEnd()) {
            return type == TokenType.EOF;
        }

        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }

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
        errors.add(
                "Error sintactico en linea " + token.getLine() +
                        ", columna " + token.getColumn() +
                        ": " + message +
                        ". Se encontro: '" + token.getLexeme() + "'"
        );
    }

    private String cleanString(String text) {
        if (text == null) return "";

        if (text.startsWith("\"") && text.endsWith("\"") && text.length() >= 2) {
            return text.substring(1, text.length() - 1);
        }

        return text;
    }
}