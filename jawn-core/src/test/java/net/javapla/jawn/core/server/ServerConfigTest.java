package net.javapla.jawn.core.server;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.*;

import org.junit.Test;

public class ServerConfigTest {

    @Test
    public void performanceEnum() {
        assertThat(ServerConfig.Performance.determineFromString("highest")).isEqualTo(ServerConfig.Performance.HIGHEST);
        assertThat(ServerConfig.Performance.determineFromString("hiGHest")).isEqualTo(ServerConfig.Performance.HIGHEST);
        
        assertThat(ServerConfig.Performance.determineFromString("minimum")).isEqualTo(ServerConfig.Performance.MINIMUM);
        
        assertThrows(IllegalArgumentException.class, () -> ServerConfig.Performance.determineFromString("73"));
    }
    
    @Test
    public void backlog() {
        ServerConfig.Impl config = new ServerConfig.Impl();
        assertThat(config.backlog()).isEqualTo(ServerConfig.Performance.MINIMUM.getBacklogValue());
        
        
        config.backlog(33);
        
        assertThat(config.backlog()).isEqualTo(33);
        assertThat(config.performance()).isEqualTo(ServerConfig.Performance.CUSTOM);
    }
    
    @Test
    public void performance() {
        ServerConfig.Impl config = new ServerConfig.Impl();
        
        assertThat(config.performance()).isEqualTo(ServerConfig.Performance.MINIMUM);
        
        config.performance(ServerConfig.Performance.HIGHEST);
        
        assertThat(config.backlog()).isEqualTo(ServerConfig.Performance.HIGHEST.getBacklogValue());
        assertThat(config.performance()).isEqualTo(ServerConfig.Performance.HIGHEST);
    }
    
    @Test
    public void port() {
        ServerConfig.Impl config = new ServerConfig.Impl();
        assertThat(config.port()).isEqualTo(8080);
        
        config.port(7000);
        assertThat(config.port()).isEqualTo(7000);
    }

    @Test
    public void context() {
        ServerConfig.Impl config = new ServerConfig.Impl();
        assertThat(config.context()).isEmpty();
        
        config.context(null);
        assertThat(config.context()).isEmpty();
        
        config.context("");
        assertThat(config.context()).isEmpty();
        
        config.context("cookie");
        assertThat(config.context()).isEqualTo("/cookie");
        
        config.context("/something");
        assertThat(config.context()).isEqualTo("/something");
        
//        config.context("///cool");
//        assertThat(config.context()).isEqualTo("/cool");
    }
}
