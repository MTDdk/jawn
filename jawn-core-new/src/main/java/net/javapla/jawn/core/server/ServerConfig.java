package net.javapla.jawn.core.server;

import java.net.ServerSocket;

public class ServerConfig {
    
    /**
     * Default value is the same as for {@link ServerSocket#bind(java.net.SocketAddress, int)}
     * @see ServerSocket#bind(java.net.SocketAddress, int)
     */
    public static final int BACKLOG_DEFAULT = -1;//50; 
    
    /**
     * Holds default values for common performance modes.
     * 
     * <p><code>HIGHEST</code> Sets the server to use best configuration for highest possible performance
     * <p><code>MINIMUM</code>
     * <p><code>CUSTOM</code> Use the user-inputted configuration, like {@link ServerConfig#ioThreads(int)}
     *
     */
    public enum PERFORMANCE { HIGHEST(8192), MINIMUM(50), CUSTOM(BACKLOG_DEFAULT);
        private final int backlog;
        private PERFORMANCE(final int backlog) {
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
    
    private PERFORMANCE performance = PERFORMANCE.MINIMUM;
    private int backlog = -1;
    
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
    
    public String host() {
        return host;
    }
    public ServerConfig host(String host) {
        this.host = host;
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
        this.performance = PERFORMANCE.CUSTOM;
        
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
        this.performance = PERFORMANCE.CUSTOM;
        
        this.backlog = backlog;
        return this;
    }
    public int backlog() {
        return backlog;
    }
    
    public PERFORMANCE serverPerformance() {
        return performance;
    }
    public ServerConfig serverPerformance(PERFORMANCE mode) {
        this.performance = mode;
        this.backlog = performance.getBacklogValue();
        return this;
    }
}
