package net.javapla.jawn.core.server;

import java.net.ServerSocket;

public class ServerConfig {
    
    /**
     * Default value is the same as for {@link ServerSocket#bind(java.net.SocketAddress, int)}
     * @see ServerSocket#bind(java.net.SocketAddress, int)
     */
    public static final int BACKLOG_DEFAULT = 50; 
    
    /**
     * Holds default values for common performance modes.
     * 
     * <p><code>HIGHEST</code> Sets the server to use best configuration for highest possible performance
     * <p><code>HIGH</code> Sets the server to use configuration for high performance
     * <p><code>MEDIUM</code> 
     * <p><code>MINIMAL</code>
     * <p><code>CUSTOM</code> Use the user-inputted configuration, like {@link ServerConfig#ioThreads(int)}
     *
     */
    public enum PERFORMANCE_MODE { HIGHEST(10_000), HIGH(1024), MEDIUM(256), LOW(50), CUSTOM(BACKLOG_DEFAULT);
        private final int backlog;
        private PERFORMANCE_MODE(final int backlog) {
            this.backlog = backlog;
        }
        public int getBacklogValue() { return backlog; }
    }
    
    private String contextPath = "";
    private int port = 8080;
    
    /** The source folder to read templates from */
    private String webapp = "src/main/webapp";
    private String host = "0.0.0.0";
    
    private int ioThreads = 1;
    
    private PERFORMANCE_MODE performance = PERFORMANCE_MODE.MEDIUM;
    private int backlog = 0;
    private boolean backlogSet = false;
    
    /**
     * @return empty string, if no context path is specified;
     *         otherwise the input context path, guaranteed to start with '/'
     */
    public String contextPath() {
        return contextPath;
    }
    public ServerConfig contextPath(String contextPath) {
        if (contextPath.charAt(0) != '/') this.contextPath = '/' + contextPath;
        else this.contextPath = contextPath;
        return this;
    }
    
    public int port() {
        return port;
    }
    public ServerConfig port(int port) {
        this.port = port;
        return this;
    }
    
    public String webappPath() {
        return webapp;
    }
    public ServerConfig webappPath(String webapp) {
        this.webapp = webapp;
        return this;
    }
    
    public int ioThreads() {
        return ioThreads;
    }
    /**
     * Automatically set server performance to PERFORMANCE_MODE#CUSTOM
     * @param number
     * @return this for chaining
     */
    public ServerConfig ioThreads(int number) {
        this.performance = PERFORMANCE_MODE.CUSTOM;
        
        if (number > 0)
            this.ioThreads = number;
        
        return this;
    }
    
    /**
     * Sets the backlog value for the ServerSocket.
     * 
     * @see ServerSocket#bind(java.net.SocketAddress, int)
     * @param backlog
     * @return this for chaining
     */
    public ServerConfig backlog(int backlog) {
        this.backlog = backlog;
        backlogSet = true;
        return this;
    }
    public int backlog() {
        return backlogSet ? backlog : performance.getBacklogValue();
    }
    
    public PERFORMANCE_MODE serverPerformance() {
        return performance;
    }
    public ServerConfig serverPerformance(PERFORMANCE_MODE mode) {
        this.performance = mode;
        return this;
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
    
    public String host() {
        return host;
    }
    public void host(String host) {
        this.host = host;
    }
    
}
