package net.javapla.jawn.core.server;

import java.net.ServerSocket;

import net.javapla.jawn.core.server.ServerConfig.Performance;

public interface SC {
    
    final class Impl implements SC {
        
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
        }
        
        private String contextPath = "";
        private int port = 8080;
        
        /** The source folder to read templates from */
        private String webapp = "src/main/webapp";
        private String host = "0.0.0.0";
        
        private int ioThreads = 1;
        
        private Performance performance = Performance.MINIMUM;
        private int backlog = -1;
        
        
        public int port() {
            return port;
        }
        
        @Override
        public SC port(int port) {
            this.port = port;
            return this;
        }

        public String context() {
            return contextPath;
        }

        @Override
        public SC context(String path) {
            this.contextPath = path;
            return this;
        }

        public String host() {
            return host;
        }

        @Override
        public SC host(String host) {
            this.host = host;
            return this;
        }

        public String webapp() {
            return webapp;
        }

        @Override
        public SC webapp(String path) {
            this.webapp = path;
            return this;
        }

        public int ioThreads() {
            return ioThreads;
        }

        @Override
        public SC ioThreads(int number) {
            this.ioThreads = number;
            return this;
        }

        public int backlog() {
            return backlog;
        }

        @Override
        public SC backlog(int number) {
            this.backlog = number;
            return this;
        }

        public Performance performance() {
            return performance;
        }

        @Override
        public SC performance(ServerConfig.Performance mode) {
            return this;
        }
    }

    SC port(int port);
    SC context/*Path*/(String path);
    SC host(String host);
    SC webapp/*Path*/(String path);
    SC ioThreads(int number);
    SC backlog(int number);
    SC performance(Performance mode);
}
