package net.javapla.jawn.core.internal.template;

import java.util.List;

public class Lox {
    
    static void run(String source) {
        LoxScanner scanner = new LoxScanner(source);
        List<Token> tokens = scanner.scan();
        
        // just printing
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    static boolean hadError = false;
    static void error(int line, String message) {
        report(line, "", message);
    }
    
    private static void report(int line, String where, String message) {
        System.err.println("[line ] " + line + " Error" + where + ": " + message);
        hadError = true;
    }
}
