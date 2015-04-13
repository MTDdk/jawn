package net.javapla.jawn.db;

import java.util.HashMap;
import java.util.Map;

import net.javapla.jawn.Modes;

public class DatabaseConnections {

    private final Map<Modes, DatabaseConnection> builders;
    
    
    public DatabaseConnections() {
        builders = new HashMap<>();
    }
    
    public DatabaseConnectionBuilderImpl environment(Modes mode) {
        DatabaseConnectionBuilderImpl bob = new DatabaseConnectionBuilderImpl(mode);
        builders.put(mode, bob);
        return bob;
    }
    
    DatabaseConnection getConnection(Modes mode) {
        return builders.get(mode);
    }
    
    public interface DatabaseConnectionBuilder {
        JdbcConnectionBuilder jdbc();
    }
    public interface DatabaseConnection {
        String url();
        String driver();
        String user();
        String password();
    }
    public interface JdbcConnectionBuilder {
        JdbcConnectionBuilder url(String url);
        JdbcConnectionBuilder driver(String driver);
        JdbcConnectionBuilder user(String user);
        JdbcConnectionBuilder password(String pass);
    }
    
    public class DatabaseConnectionBuilderImpl implements DatabaseConnectionBuilder, DatabaseConnection {
        final Modes mode;
        String driver;
        String url;
        String user;
        String password;
        
        public DatabaseConnectionBuilderImpl(Modes mode) {
            this.mode = mode;
        }
        
        public JdbcConnectionBuilder jdbc() {
            return new JdbcConnectionBuilderImpl(this);
        }
        
        public String driver() {
            return driver;
        }
        public String url() {
            return url;
        }
        public String user() {
            return user;
        }
        public String password() {
            return password;
        }
    }
    
    private class JdbcConnectionBuilderImpl implements JdbcConnectionBuilder {
        private final DatabaseConnectionBuilderImpl builder;
        public JdbcConnectionBuilderImpl(DatabaseConnectionBuilderImpl builder) {
            this.builder = builder;
        }
        
        public JdbcConnectionBuilder url(String url) {
            builder.url = url;
            return this;
        }
        public JdbcConnectionBuilder driver(String driver) {
            builder.driver = driver;
            return this;
        }
        public JdbcConnectionBuilder user(String user) {
            builder.user = user;
            return this;
        }
        public JdbcConnectionBuilder password(String pass) {
            builder.password = pass;
            return this;
        }
    }
}
