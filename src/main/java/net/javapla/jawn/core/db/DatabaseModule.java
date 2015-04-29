package net.javapla.jawn.core.db;

import net.javapla.jawn.core.PropertiesImpl;

import com.google.inject.AbstractModule;

public class DatabaseModule extends AbstractModule {

    private DatabaseConnections connections;
    private PropertiesImpl properties;

    public DatabaseModule(DatabaseConnections connections, PropertiesImpl properties) {
        this.connections = connections;
        this.properties = properties;
    }
    
    @Override
    protected void configure() {
        
        DatabaseConnection connection = connections.getConnection(properties.getMode());
        
        if (connection != null)
            bind(DatabaseConnection.class).toInstance(connection);
    }

}
