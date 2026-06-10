package org.example;

import java.io.*;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        String inputFile = "fuente.txt";
        String outputXmlFile = "resultado.xml";
        String outputErrorFile = "resultado.txt";

        Lexer lexer = new Lexer();
        List<Token> allTokens = new ArrayList<>();
        List<String> lexicalErrors = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {

            String line;
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                try {
                    allTokens.addAll(lexer.tokenizeLine(line, lineNumber));
                } catch (LexicalException e) {
                    lexicalErrors.add(e.getMessage());
                }

                lineNumber++;
            }

            allTokens.add(new Token(TokenType.EOF, "EOF", lineNumber, 1));

            Traductor traductor = new Traductor(allTokens);
            String xml = traductor.parseToXml();

            if (lexicalErrors.isEmpty() && traductor.getErrors().isEmpty()) {

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputXmlFile))) {
                    bw.write(xml);
                }

                System.out.println("Traduccion completada correctamente.");
                System.out.println("Archivo XML generado: " + outputXmlFile);

            } else {
                System.out.println("Fuente incorrecta.");
                System.out.println("Errores guardados en: " + outputErrorFile);

                try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputErrorFile))) {
                    bw.write("Fuente incorrecta.");
                    bw.newLine();

                    for (String error : lexicalErrors) {
                        System.out.println(error);
                        bw.write(error);
                        bw.newLine();
                    }

                    for (String error : traductor.getErrors()) {
                        System.out.println(error);
                        bw.write(error);
                        bw.newLine();
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error de archivo: " + e.getMessage());
        }
    }
}