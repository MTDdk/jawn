package net.javapla.jawn.core.server;

import java.net.ServerSocket;

public interface ServerConfig {
    
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
    public enum Performance { HIGHEST(8192), MINIMUM(50), CUSTOM(BACKLOG_DEFAULT);
        private final int backlog;
        private Performance(final int backlog) {
            this.backlog = backlog;
        }
        public int getBacklogValue() { return backlog; }
        
        public static Performance determineFromString(final String performance) throws IllegalArgumentException {
            final String p = performance.toLowerCase();
            for (Performance perf : Performance.values()) {
                if (perf.toString().equals(p) || perf.name().toLowerCase().equals(p)) 
                    return perf;
            }
            throw new IllegalArgumentException("No enum constant " + Performance.class.getName() + "." + performance);
        }
    }
    
    final class Impl implements ServerConfig {
        
        
        private String contextPath = "";
        private int port = 8080;
        
        ///** The source folder to read templates from */
        //private String webapp = "src/main/webapp";//TODO read from configuration
        private String host = "0.0.0.0";
        
        private int ioThreads = 1;
        
        private Performance performance = Performance.MINIMUM;
        private int backlog = BACKLOG_DEFAULT;
        
        public Impl() {
            performance(Performance.MINIMUM);
        }
        
        
        public int port() {
            return port;
        }
        
        @Override
        public ServerConfig port(int port) {
            this.port = port;
            return this;
        }

        /**
         * @return empty string, if no context path is specified;
         *         otherwise the input context path, guaranteed to start with '/'
         */
        public String context() {
            return contextPath;
        }

        @Override
        public ServerConfig context(String path) {
            if (path == null || path.isEmpty()) return this;
            
            if (path.charAt(0) != '/') this.contextPath = '/' + path;
            else this.contextPath = path;
            return this;
        }

        public String host() {
            return host;
        }

        @Override
        public ServerConfig host(String host) {
            this.host = host;
            return this;
        }

        /*public String webapp() {
            return webapp;
        }

        @Override
        public ServerConfig webapp(String path) {
            this.webapp = path;
            return this;
        }*/

        public int ioThreads() {
            return ioThreads;
        }

        @Override
        public ServerConfig ioThreads(int number) {
            if (number > 0) {
                this.performance = Performance.CUSTOM;
                this.ioThreads = number;
            }
            return this;
        }

        public int backlog() {
            return backlog;
        }

        @Override
        public ServerConfig backlog(int number) {
            this.performance = Performance.CUSTOM;
            this.backlog = number;
            return this;
        }

        public Performance performance() {
            return performance;
        }

        @Override
        public ServerConfig performance(Performance mode) {
            this.performance = mode;
            this.backlog = mode.getBacklogValue();
            return this;
        }
    }

    ServerConfig port(int port);
    ServerConfig context/*Path*/(String path);
    ServerConfig host(String host);
    //ServerConfig webapp/*Path*/(String path);
    /**
     * Automatically set server performance to {@link Performance#CUSTOM}
     * @param number
     * @return
     */
    ServerConfig ioThreads(int number);
    /**
     * Sets the backlog value for the underlying ServerSocket.
     * Automatically set server performance to {@link Performance#CUSTOM}
     * @see ServerSocket#bind(java.net.SocketAddress,int)
     * @param number
     * @return
     */
    ServerConfig backlog(int number);
    ServerConfig performance(Performance mode);
}
