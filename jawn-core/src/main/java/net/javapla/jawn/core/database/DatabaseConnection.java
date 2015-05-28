package net.javapla.jawn.core.database;

import java.beans.PropertyVetoException;

import javax.sql.DataSource;

public interface DatabaseConnection {
    String url();
    String driver();
    String user();
    String password();
    
    int maxPoolSize();
    int minPoolSize();
    
    /**
     * 
     * @return
     * @throws ClassNotFoundException If the driver was not found
     * @throws PropertyVetoException If any of the input is unacceptable
     */
    DataSource createDataSource() throws ClassNotFoundException, PropertyVetoException;
}