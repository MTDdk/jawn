package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Injector;

import net.javapla.jawn.core.Modes;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.server.ServerConfig;

public class FrameworkBootstrapTest {
    
    static ServerConfig.Impl serverConfig;
    static Function<Injector, List<Route>> routes;
    
    static FrameworkBootstrap framework;
    
    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void setUpBeforeClass() throws Exception {
        serverConfig = new ServerConfig.Impl();
        routes = mock(Function.class);
        when(routes.apply(any(Injector.class))).thenReturn(List.of());
    }
    
    @Before
    public void before() {
        framework = new FrameworkBootstrap();
    }

    @Test
    public void alreadyBooted() {
        framework.boot(Modes.TEST, serverConfig, routes);
        assertThrows(RuntimeException.class, () -> framework.boot(Modes.TEST, serverConfig, routes));
    }
    
    @Test
    public void userModule() {
        AtomicBoolean called = new AtomicBoolean(false);
        AtomicBoolean startup = new AtomicBoolean(false);
        AtomicBoolean shutdown = new AtomicBoolean(false);
        
        framework.register(applicationConfig -> {
            called.set(true);
            assertThat(applicationConfig.mode()).isEqualTo(Modes.TEST);
            
            applicationConfig.onStartup(() -> startup.set(true));
            applicationConfig.onShutdown(() -> shutdown.set(true));
        });
        
        framework.boot(Modes.TEST, serverConfig, routes);
        assertThat(called.get()).isTrue();
        assertThat(startup.get()).isTrue();
        assertThat(shutdown.get()).isFalse();
        
        framework.shutdown();
        assertThat(shutdown.get()).isTrue();
        assertThat(framework.getInjector()).isNull();
    }
    
    @Test
    public void notInitialised() {
        assertThat(framework.getInjector()).isNull();
        
        AtomicBoolean shutdown = new AtomicBoolean(false);
        framework.onShutdown(() -> shutdown.set(true) );
        framework.shutdown();

        assertThat(framework.getInjector()).isNull();
        assertThat(shutdown.get()).isFalse();
    }
    
    @Test
    public void shutdown_throws() {
        boolean[] shutdowns = {false, false};
        framework.onShutdown(() -> shutdowns[0] = true );
        framework.onShutdown(() -> {throw new RuntimeException();} );
        framework.onShutdown(() -> shutdowns[1] = true );
        
        framework.boot(Modes.TEST, serverConfig, routes);
        framework.shutdown();
        
        assertThat(framework.getInjector()).isNull();
        assertThat(shutdowns[0]).isTrue();
        assertThat(shutdowns[1]).isTrue();
    }
    
    @Test
    public void startup_throws() {
        boolean[] startups = {false, false};
        framework.onStartup(() -> startups[0] = true );
        framework.onStartup(() -> {throw new RuntimeException();} );
        framework.onStartup(() -> startups[1] = true );
        
        framework.boot(Modes.TEST, serverConfig, routes);
        
        assertThat(framework.getInjector()).isNotNull();
        assertThat(startups[0]).isTrue();
        assertThat(startups[1]).isTrue();
    }
    
    @Test
    public void reboot() {
        @SuppressWarnings("unchecked")
        Function<Injector, List<Route>> nroutes = mock(Function.class);
        when(nroutes.apply(any(Injector.class))).thenReturn(List.of());
        
        framework.boot(Modes.TEST, serverConfig, routes);
        framework.reboot___strap(nroutes);
        
        verify(nroutes, times(1)).apply(framework.injector);
    }
}
