package net.javapla.jawn.core.internal.template.lox;

import java.util.List;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    
    public static void main(String[] args) {
        
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }
    
    static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        
        // Stop if there was a syntax error
        if (hadError) return;
        
        interpreter.interpret(statements);
    }
    
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
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
    
    static void runtimeError(RuntimeError e) {
        System.err.println(e.getMessage() + "\n[line " + e.token.line + "]");
        hadRuntimeError = true;
    }
}
