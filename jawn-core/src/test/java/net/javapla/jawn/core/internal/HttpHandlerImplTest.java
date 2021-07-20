package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

import net.javapla.jawn.core.Context;
import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.Results;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.SessionStore;
import net.javapla.jawn.core.internal.renderers.RendererEngineOrchestratorImplTest;
import net.javapla.jawn.core.renderers.RendererEngineOrchestrator;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.server.ServerResponse;

public class HttpHandlerImplTest {
    
    private static final RendererEngineOrchestrator RENDERERS = mock(RendererEngineOrchestrator.class);
    
    static Injector injector;
    static ResultRunner runner;
    static SessionStore sessionStore;
    static DeploymentInfo di;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        injector = Guice.createInjector(RendererEngineOrchestratorImplTest.rendererModule());
        sessionStore = mock(SessionStore.class);
        runner = injector.getInstance(ResultRunner.class);
        
        
        di = mock(DeploymentInfo.class);
        when(di.getRealPath("")).thenReturn("");
        when(di.stripContextPath(anyString())).then(returnsFirstArg());
        
//        injector = mock(Injector.class);
//        when(injector.getInstance(DeploymentInfo.class)).thenReturn(di);
    }
    

    @Test
    public void handle() throws Exception {
        String path = "/test";
        
        Route.Handler routeHandler = mock(Route.Handler.class);
        when(routeHandler.handle(any(Context.class))).thenReturn(Results.ok());
        when(routeHandler.then(any(Route.After.class))).thenCallRealMethod();
        
        
        Router router = new Router()
            .compileRoutes(Arrays.asList(new Route.BuilderImpl(HttpMethod.GET, path, routeHandler).build(RENDERERS)));
        
        
        HttpHandlerImpl handler = 
            new HttpHandlerImpl(StandardCharsets.UTF_8, router, runner, di, sessionStore, injector);
        
        
        ServerRequest request = mock(ServerRequest.class);
        when(request.path()).thenReturn(path);
        when(request.method()).thenReturn(HttpMethod.GET);
        
        ServerResponse response = mock(ServerResponse.class);
        
        handler.handle(request, response);
        
        verify(request, times(1)).path();
        verify(request, times(2)).method(); // Router#retrieve + ResultRunner#execute
        
        verify(routeHandler, times(1)).handle(any(Context.class));
        
        verify(response, times(2)).committed();
        verify(response, times(1)).end();
    }
    
    @Test
    public void endingSlash_not_needed() throws Exception {
        Route.Handler routeHandler = mock(Route.Handler.class);
        when(routeHandler.handle(any(Context.class))).thenReturn(Results.ok());
        when(routeHandler.then(any(Route.After.class))).thenCallRealMethod();
        
        
        Router router = new Router()
            .compileRoutes(Arrays.asList(
                new Route.BuilderImpl(HttpMethod.GET, "/first", routeHandler).build(RENDERERS),
                new Route.BuilderImpl(HttpMethod.GET, "/first/second", routeHandler).build(RENDERERS),
                new Route.BuilderImpl(HttpMethod.GET, "/", routeHandler).build(RENDERERS)
            ));
        
        
        HttpHandlerImpl handler = 
            new HttpHandlerImpl(StandardCharsets.UTF_8, router, runner, di, sessionStore, injector);
        
        
        ServerRequest request = mock(ServerRequest.class);
        when(request.path()).thenReturn("/first/", "/first/second/", "/");
        when(request.method()).thenReturn(HttpMethod.GET);
        
        ServerResponse response = mock(ServerResponse.class);
        
        handler.handle(request, response);
        handler.handle(request, response);
        handler.handle(request, response);
        
        verify(request, times(1 * 3)).path();
        verify(request, times(2 * 3)).method(); // Router#retrieve + ResultRunner#execute
        
        verify(routeHandler, times(1 * 3)).handle(any(Context.class));
        
        verify(response, times(2 * 3)).committed();
        verify(response, times(1 * 3)).end();
    }
    
    @Test
    public void normalise() {
        assertThat(HttpHandlerImpl.normaliseURI("/first/")).isEqualTo("/first");
    }
}
