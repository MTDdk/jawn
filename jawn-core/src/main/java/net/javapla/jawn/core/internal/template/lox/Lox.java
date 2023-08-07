package net.javapla.jawn.core.internal.template.lox;

import java.util.List;

public class Lox {
    
    static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scan();
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();
        
        // Stop if there was a syntax error
        if (hadError) return;
        
        System.out.println(expression);
        
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
    
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, "at '" + token.lexeme + "'", message);
        }
    }
}
