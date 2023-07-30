package net.javapla.jawn.core.internal.template;

public class Lox {

    static boolean hadError = false;
    static void error(int line, String message) {
        report(line, "", message);
    }
    
    private static void report(int line, String where, String message) {
        System.err.println("[line ] " + line + " Error" + where + ": " + message);
        hadError = true;
    }
}
