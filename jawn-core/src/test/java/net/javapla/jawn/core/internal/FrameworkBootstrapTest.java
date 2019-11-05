package net.javapla.jawn.core.internal;

import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import com.google.inject.Injector;

import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.server.ServerConfig;
import net.javapla.jawn.core.util.Modes;

public class FrameworkBootstrapTest {

    @Test
    @SuppressWarnings("unchecked")
    public void alreadyBooted() {
        ServerConfig.Impl config = new ServerConfig.Impl();
        Function<Injector, List<Route>> routes = mock(Function.class);
        when(routes.apply(any(Injector.class))).thenReturn(List.of());
        
        
        FrameworkBootstrap framework = new FrameworkBootstrap();
        
        framework.boot(Modes.TEST, config, routes);
        assertThrows(RuntimeException.class, () -> framework.boot(Modes.TEST, config, routes));
    }
    
    @Test
    public void locatePluginModule() {
        
    }

}
