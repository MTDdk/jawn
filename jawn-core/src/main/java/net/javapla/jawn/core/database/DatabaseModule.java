package net.javapla.jawn.core.database;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;

import net.javapla.jawn.core.configuration.JawnConfigurations;

public class DatabaseModule extends AbstractModule {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private DatabaseConnections connections;
    private JawnConfigurations properties;

    public DatabaseModule(DatabaseConnections connections, JawnConfigurations properties) {
        this.connections = connections;
        this.properties = properties;
    }
    
    @Override
    protected void configure() {
        if (connections.hasConnections()) {
            DatabaseConnection connection = connections.getConnection(properties.getMode());
            
            if (connection != null) {
                bind(DatabaseConnection.class).toInstance(connection);
                bind(DataSource.class).toInstance(connection); // DatabaseConnection implements DataSource
            } else {
                logger.error("No {} has been configured to current mode of '{}' but other modes has been configured.\n"
                    + "\tYou are probably gonna get a problem with dependency injection", DatabaseConnection.class.getSimpleName(), properties.getMode());
            }
        } else {
        /*bind(DatabaseConnection.class).toProvider(Providers.of(null)).asEagerSingleton();
            bind(DataSource.class).toProvider(Providers.of(null)).asEagerSingleton();*/
        }
        
        
    }
}
