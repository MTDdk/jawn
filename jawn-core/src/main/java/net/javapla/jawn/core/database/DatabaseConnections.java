package net.javapla.jawn.core.database;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import net.javapla.jawn.core.util.Modes;

import com.mchange.v2.c3p0.ComboPooledDataSource;

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
    public interface JdbcConnectionBuilder {
        JdbcConnectionBuilder url(String url);
        JdbcConnectionBuilder driver(String driver);
        JdbcConnectionBuilder user(String user);
        JdbcConnectionBuilder password(String pass);
        
        JdbcConnectionBuilder maxPoolSize(int max);
        JdbcConnectionBuilder minPoolSize(int min);
    }
    
    public class DatabaseConnectionBuilderImpl implements DatabaseConnectionBuilder, DatabaseConnection {
        final Modes mode;
        String driver;
        String url;
        String user;
        String password;
        int maxPoolSize = 8;
        int minPoolSize = 1;
        
        public DatabaseConnectionBuilderImpl(Modes mode) {
            this.mode = mode;
        }
        
        @Override
        public JdbcConnectionBuilder jdbc() {
            return new JdbcConnectionBuilderImpl(this);
        }
        
        @Override
        public String driver() {
            return driver;
        }
        
        @Override
        public String url() {
            return url;
        }
        
        @Override
        public String user() {
            return user;
        }
        
        @Override
        public String password() {
            return password;
        }
        
        @Override
        public int maxPoolSize() { return maxPoolSize; }
        @Override
        public int minPoolSize() { return minPoolSize; }
        
        @Override
        public DataSource createDataSource() throws PropertyVetoException, ClassNotFoundException {
            Class.forName(driver());
            
            ComboPooledDataSource source = new ComboPooledDataSource();
            source.setDriverClass(driver());
            source.setJdbcUrl(url());
            source.setUser(user());
            source.setPassword(password());
            
            source.setMaxPoolSize(maxPoolSize());
            source.setMinPoolSize(minPoolSize());
            source.setAcquireIncrement(1);
            source.setIdleConnectionTestPeriod(300);
            source.setMaxStatements(0);
            
            return source;
        }
    }
    
    //JdbcConnectionSpec
    private class JdbcConnectionBuilderImpl implements JdbcConnectionBuilder {
        private final DatabaseConnectionBuilderImpl builder;
        public JdbcConnectionBuilderImpl(DatabaseConnectionBuilderImpl builder) {
            this.builder = builder;
        }
        
        @Override
        public JdbcConnectionBuilder url(String url) {
            builder.url = url;
            return this;
        }
        
        @Override
        public JdbcConnectionBuilder driver(String driver) {
            builder.driver = driver;
            return this;
        }
        
        @Override
        public JdbcConnectionBuilder user(String user) {
            builder.user = user;
            return this;
        }
        
        @Override
        public JdbcConnectionBuilder password(String pass) {
            builder.password = pass;
            return this;
        }
        
        @Override
        public JdbcConnectionBuilder maxPoolSize(int max) {
            builder.maxPoolSize = max;
            return this;
        }
        
        @Override
        public JdbcConnectionBuilder minPoolSize(int min) {
            builder.minPoolSize = min;
            return this;
        }
    }
}
