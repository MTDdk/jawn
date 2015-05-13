package net.javapla.jawn.server.spi;

import net.javapla.jawn.core.util.Modes;

public class ServerConfig {
    
    private String contextPath = "/";
    private int port = 8080;
    
    /** The source folder to read templates from */
    private String webapp = "src/main/webapp";
    private String environment = Modes.dev.toString();
    
    
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
    
    

}
