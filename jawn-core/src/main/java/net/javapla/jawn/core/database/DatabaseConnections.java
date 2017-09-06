package net.javapla.jawn.core.database;

import java.beans.PropertyVetoException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.EnumMap;
import java.util.logging.Logger;

import com.zaxxer.hikari.HikariDataSource;

import net.javapla.jawn.core.util.Modes;

public class DatabaseConnections {

    private final EnumMap<Modes, DatabaseConnectionBuilderImpl> builders;
    
    
    public DatabaseConnections() {
        builders = new EnumMap<>(Modes.class);
    }
    
    public DatabaseConnectionBuilderImpl environment(Modes mode) {
        DatabaseConnectionBuilderImpl bob = new DatabaseConnectionBuilderImpl(mode);
        builders.put(mode, bob);
        return bob;
    }
    
    DatabaseConnection getConnection(Modes mode) {
        DatabaseConnectionBuilderImpl connection = builders.get(mode);
        if (connection == null) return null;
        
        if (connection.letFrameworkHandleConnectionPool())
            connection.initiatePooledDataSource();
        return connection;
    }
    boolean hasConnections() {
        return !builders.isEmpty();
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
        JdbcConnectionBuilder letFrameworkHandleConnectionPool(boolean letFramework);
    }
    
    public class DatabaseConnectionBuilderImpl implements DatabaseConnectionBuilder, DatabaseConnection {
        final Modes mode;
        String driver;
        String url;
        String user;
        String password;
        int maxPoolSize = Runtime.getRuntime().availableProcessors() * 2 + 1;
        boolean letFrameworkHandleConnectionPool = false;
        
        
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
        public boolean letFrameworkHandleConnectionPool() { return letFrameworkHandleConnectionPool; }
        
        /**
         * Creates a pooled DataSource to be used in a somewhat high performance use case,
         * like database managers that are used throughout the application.
         * 
         * @return
         * @throws ClassNotFoundException If the driver was not found
         * @throws PropertyVetoException If any of the input is unacceptable
         */
        private HikariDataSource createPooledDataSource() throws PropertyVetoException, ClassNotFoundException {
            Class.forName(driver());
            
            HikariDataSource source = new HikariDataSource();
            source.setDriverClassName(driver());
            source.setJdbcUrl(url());
            source.setUsername(user());
            source.setPassword(password());
            
            source.setMaximumPoolSize(maxPoolSize());
            
            return source;
        }
        
        /* ***************
         * DataSource Part
         * ****************/
        volatile HikariDataSource/*ComboPooledDataSource*/ source;
        private final Object lock = new Object();
        void initiatePooledDataSource() {
            if (source == null) {
                synchronized(lock) {
                    if (source == null) {
                        try {
                            source = createPooledDataSource();
                        } catch (ClassNotFoundException | PropertyVetoException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        
        @Override
        public Connection getConnection() throws SQLException {
            return source.getConnection();
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            return source.getConnection(username, password);
        }

        @Override
        public PrintWriter getLogWriter() throws SQLException {
            return source.getLogWriter();
        }

        @Override
        public void setLogWriter(PrintWriter out) throws SQLException {
            source.setLogWriter(out);
        }

        @Override
        public void setLoginTimeout(int seconds) throws SQLException {
            source.setLoginTimeout(seconds);
        }

        @Override
        public int getLoginTimeout() throws SQLException {
            return source.getLoginTimeout();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return source.getParentLogger();
        }

        @Override
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return source.unwrap(iface);
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return source.isWrapperFor(iface);
        }

        @Override
        public void close() throws Exception {
            source.close();
        }
    }
    
    
    //JdbcConnectionSpec
    private static class JdbcConnectionBuilderImpl implements JdbcConnectionBuilder {
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
        public JdbcConnectionBuilder letFrameworkHandleConnectionPool(boolean letFramework) {
            builder.letFrameworkHandleConnectionPool = letFramework;
            return this;
        }
    }
}
