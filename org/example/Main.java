package org.example;

import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String inputFile = "C:\\Users\\benja\\OneDrive\\Documents\\2026-1\\fuente.txt";

        Lexer lexer = new Lexer();
        List<Token> allTokens = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                try {
                 allTokens.addAll(lexer.tokenizeLine(line, lineNumber));
                } catch (LexicalException e) {
                    System.out.println(e.getMessage());
                }
                lineNumber++;
            }

            allTokens.add(new Token(TokenType.EOF, "EOF", lineNumber, 1));

            Parser parser = new Parser(allTokens);
            parser.parse();

        } catch (IOException e) {
            System.out.println("Error de archivo: " + e.getMessage());
        }
    }
}