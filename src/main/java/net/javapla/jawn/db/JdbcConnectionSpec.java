package net.javapla.jawn.db;

import java.text.MessageFormat;

public class JdbcConnectionSpec implements ConnectionSpec<JdbcConnectionSpec> {

    private String driver;
    public JdbcConnectionSpec driver(String driver) { this.driver = driver; return this; }
    public String driver() { return driver; }
    
    
    private String url;
    public JdbcConnectionSpec url(String url) { this.url = url; return this; }
    public String url() throws IllegalArgumentException {
        if (this.url == null) throw new IllegalArgumentException("JDBC url is null");
        return this.url;
    }
    
    
    private String user;
    public JdbcConnectionSpec user(String user) { this.user = user; return this; }
    public String user() { return this.user; }
    
    
    private String password;
    public JdbcConnectionSpec password(String password) { this.password = password; return this; }
    public String password() { return this.password; }

    
    public JdbcConnectionSpec() {}
    
    public JdbcConnectionSpec(String driver, String url, String user, String password) {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }
    public JdbcConnectionSpec(String driver, String url) {
        this(driver, url, null, null);
    }
    
    @Override
    public String toString() {
        return MessageFormat.format("driver: {0}, url: {1}, user: {2}, password: {3}", driver, url, user, password);
    }

    @Override
    public JdbcConnectionSpec get() {
        return this;
    }
}
