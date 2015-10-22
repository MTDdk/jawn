package net.javapla.jawn.server.spi;

import java.net.ServerSocket;

import net.javapla.jawn.core.util.Modes;

public class ServerConfig {
    
    /**
     * <p><code>HIGHEST</code> Sets the server to use best configuration for highest possible performance
     * <p><code>HIGH</code> Sets the server to use configuration for high performance
     * <p><code>MEDIUM</code> 
     * <p><code>MINIMAL</code>
     * <p><code>CUSTOM</code> Use the user-inputted configuration, like {@link ServerConfig#setIoThreads(int)}
     *
     */
    public enum PERFORMANCE_MODE { HIGHEST, HIGH, MEDIUM, MINIMAL, CUSTOM }
    
    private String contextPath = "/";
    private int port = 8080;
    
    /** The source folder to read templates from */
    private String webapp = "src/main/webapp";
    private String environment = Modes.DEV.toString();
    private String host = "0.0.0.0";
    
    private int ioThreads = 1;
    
    private int backlog = 50; // default value used in ServerSocket
    private PERFORMANCE_MODE performance = PERFORMANCE_MODE.MEDIUM;
    
    
    public String getContextPath() {
        return contextPath;
    }
    public ServerConfig setContextPath(String contextPath) {
        this.contextPath = contextPath;
        return this;
    }
    
    public int getPort() {
        return port;
    }
    public ServerConfig setPort(int port) {
        this.port = port;
        return this;
    }
    
    public String getWebappPath() {
        return webapp;
    }
    public ServerConfig setWebappPath(String webapp) {
        this.webapp = webapp;
        return this;
    }
    
    public String getEnvironment() {
        return environment;
    }
    public ServerConfig setEnvironment(String environment) {
        this.environment = environment;
        return this;
    }
    public ServerConfig setEnvironment(Modes mode) {
        this.environment = mode.toString();
        return this;
    }

    public int getIoThreads() {
        return ioThreads;
    }
    /**
     * Automatically set server performance to PERFORMANCE_MODE#CUSTOM
     * @param number
     * @return 
     */
    public ServerConfig setIoThreads(int number) {
        this.performance = PERFORMANCE_MODE.CUSTOM;
        
        if (number > 0)
            this.ioThreads = number;
        
        return this;
    }
    
    /**
     * Automatically set server performance to PERFORMANCE_MODE#CUSTOM.
     * 
     * Default value is the same as for {@link ServerSocket#bind(java.net.SocketAddress, int)}
     * 
     * @see ServerSocket#bind(java.net.SocketAddress, int)
     * @param backlog
     */
    public ServerConfig setBacklog(int backlog) {
        this.performance = PERFORMANCE_MODE.CUSTOM;
        
        this.backlog = backlog;
        
        return this;
    }
    public int getBacklog() {
        return backlog;
    }
    
    public PERFORMANCE_MODE getServerPerformance() {
        return performance;
    }
    public void setServerPerformance(PERFORMANCE_MODE mode) {
        this.performance = mode;
    }

    private boolean useauthentication = false;
    private String authenticationFilterUrlMapping = "/*";
    public boolean useAuthentication() {
        return this.useauthentication;
    }
    public void useAuthentication(boolean secure) {
        this.useauthentication = secure;
    }
    public String getAuthenticationFilterUrlMapping() {
        return this.authenticationFilterUrlMapping;
    }
    public void setAuthenticationFilterUrlMapping(String url) {
        this.authenticationFilterUrlMapping = url;
    }
    
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    
}
