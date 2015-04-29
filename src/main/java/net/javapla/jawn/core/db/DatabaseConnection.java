package net.javapla.jawn.core.db;

public interface DatabaseConnection {
    String url();
    String driver();
    String user();
    String password();
}