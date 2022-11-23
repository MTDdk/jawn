package net.javapla.jawn.core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.Objects;
import java.util.Optional;

import com.typesafe.config.Config;

import net.javapla.jawn.core.util.StreamUtil;

public interface Server {
    
    Server start(ServerConfig config, Plugin.Application application);
    
    Server stop();
    
    static boolean connectionResetByPeer(final Throwable cause) {
        return Optional.ofNullable(cause)
            .filter(IOException.class::isInstance)
            .map(x -> x.getMessage())
            .filter(Objects::nonNull)
            .map(String::toLowerCase)
            .map(message -> message.contains("reset by peer") || message.contains("broken pipe"))
            .orElse(cause instanceof ClosedChannelException || cause instanceof EOFException);
    }
    
    // TODO 
    static boolean isEntityBodyAllowed(int code) {
        if (code >= 100 && code < 200) {
            return false;
        }
        if (code == 204 || code == 304) {
            return false;
        }
        return true;
    }

    
    public static class ServerConfig {
        
        public static final int BACKLOG = 8192;
        
        public static final int IO_THREADS = Runtime.getRuntime().availableProcessors() * 2;
        
        
        private String host = "0.0.0.0";
        private int port = 8080;
        
        private int backlog = -1;
        private int ioThreads = -1;
        private int workerThreads = -1;
        private boolean serveDefaultHeaders = true;
        private int bufferSize = StreamUtil._16KB;
        private long maxRequestSize = 10_485_760; // 10MB
        
        
        
        public String host() {
            return host;
        }
        
        public int port() {
            return port;
        }
        
        public int backlog() {
            return backlog(BACKLOG);
        }
        
        public int backlog(int fallbackBacklog) {
            return backlog < 1 ? fallbackBacklog : backlog;
        }
        
        public int ioThreads() {
            return ioThreads(IO_THREADS);
        }
        
        public int ioThreads(int fallbackIOThreads) {
            return ioThreads < 1 ? fallbackIOThreads : ioThreads;
        }
        
        public int workerThreads() {
            return workerThreads < 1 ? ioThreads() * 8 : workerThreads;
        }
        
        /**
         * Configure server to serve default headers:
         * <ul>
         *   <li><code>Date</code>
         *   <li><code>Content-Type</code>
         *   <li><code>Server</code>
         * </ul>
         */
        public boolean serverDefaultHeaders() {
            return serveDefaultHeaders;
        }
        
        public int bufferSize() {
            return bufferSize;
        }
        
        public long maxRequestSize() {
            return maxRequestSize;
        }
        
        public static ServerConfig from(Config config) {
            ServerConfig options = new ServerConfig();
            
            if (config != null && config.hasPath("server")) {
                if (config.hasPath("server.bufferSize")) {
                    options.bufferSize = config.getInt("server.bufferSize");
                }
                if (config.hasPath("server.maxRequestSize")) {
                    options.maxRequestSize = config.getLong("server.maxRequestSize");
                }
            }
            
            return options;
        }
    }
    
}
