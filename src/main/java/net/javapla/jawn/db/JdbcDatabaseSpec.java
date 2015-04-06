package net.javapla.jawn.db;

import java.text.MessageFormat;

import net.javapla.jawn.exceptions.InitException;

public class JdbcDatabaseSpec {
    
    
    private String driver;
    public JdbcDatabaseSpec driver(String driver) { this.driver = driver; return this; }
    public String driver() { return driver; }
    
    
    private String url;
    public JdbcDatabaseSpec url(String url) { this.url = url; return this; }
    public String url() {
        if (this.url == null) throw new InitException("JDBC url is null");
        return this.url;
    }
    
    
    private String user;
    public JdbcDatabaseSpec user(String user) { this.user = user; return this; }
    public String user() { return this.user; }
    
    
    private String password;
    public JdbcDatabaseSpec password(String password) { this.password = password; return this; }
    public String password() { return this.password; }

    
    public JdbcDatabaseSpec() {}
    
    public JdbcDatabaseSpec(String driver, String url, String user, String password) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }
    public JdbcDatabaseSpec(String driver, String url) {
        this(driver, url, null, null);
    }
    
    @Override
    public String toString() {
        return MessageFormat.format("driver: {0}, url: {1}, user: {2}, password: {3}", driver, url, user, password);
    }
}
