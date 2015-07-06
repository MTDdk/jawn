package net.javapla.jawn.server.spi;

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
    private String environment = Modes.dev.toString();
    private String host = "0.0.0.0";
    
    private int ioThreads = 1;
    private PERFORMANCE_MODE performance = PERFORMANCE_MODE.MINIMAL;
    
    
    public String getContextPath() {
        return contextPath;
    }
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
    
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getWebappPath() {
        return webapp;
    }
    public void setWebappPath(String webapp) {
        this.webapp = webapp;
    }
    
    public String getEnvironment() {
        return environment;
    }
    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    public void setEnvironment(Modes mode) {
        this.environment = mode.toString();
    }

    public int getIoThreads() {
        return ioThreads;
    }
    /**
     * Automatically set server performance to PERFORMANCE_MODE#CUSTOM
     * @param number
     */
    public void setIoThreads(int number) {
        this.performance = PERFORMANCE_MODE.CUSTOM;
        
        if (number > 0)
            this.ioThreads = number;
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
