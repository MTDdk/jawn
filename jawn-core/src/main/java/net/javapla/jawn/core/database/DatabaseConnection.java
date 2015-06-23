package net.javapla.jawn.core.database;

import javax.sql.DataSource;

public interface DatabaseConnection extends DataSource {
    String url();
    String driver();
    String user();
    String password();
    
    int maxPoolSize();
    int minPoolSize();
    
    //Connection getConnection() throws SQLException;
}