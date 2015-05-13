package net.javapla.jawn.core.database;

public interface DatabaseConnection {
    String url();
    String driver();
    String user();
    String password();
}