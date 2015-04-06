package net.javapla.jawn;

import net.javapla.jawn.db.ConnectionSpec;

/**
 *
 * @author Max Artyukhov
 */
@Deprecated
public class ConnectionSpecWrapper<T> {

    private String environment;
    private String dbName = "default";    
    private boolean testing = false;    
    private ConnectionSpec<T> connectionSpec;

    public T getConnectionSpec() {
        return connectionSpec.get();
    }

    void setConnectionSpec(ConnectionSpec<T> connectionSpec) {
        this.connectionSpec = connectionSpec;
    }

    public String getDbName() {
        return dbName;
    }

    void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getEnvironment() {
        return environment;
    }

    void setEnvironment(String environment) {
        this.environment = environment;
    }

    public boolean isTesting() {
        return testing;
    }

    void setTesting(boolean testing) {
        this.testing = testing;
    }    

}
