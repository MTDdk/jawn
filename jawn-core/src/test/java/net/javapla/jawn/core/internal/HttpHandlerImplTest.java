package net.javapla.jawn.core.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.Ignore;
import org.junit.Test;

import com.google.inject.Guice;

import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.HttpMethod;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.Route;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.server.ServerResponse;

public class HttpHandlerImplTest {

    

    @Test
    @Ignore
    public void handle() throws Exception {
        Router router = mock(Router.class);
        when(router.retrieve(any(), any())).thenReturn(new Route.Builder(HttpMethod.GET).path("/test").build());
        
        HttpHandlerImpl handler = new HttpHandlerImpl(StandardCharsets.UTF_8, router, mock(ResultRunner.class), mock(DeploymentInfo.class), Guice.createInjector());
        
        ServerRequest request = mock(ServerRequest.class);
        when(request.path()).thenReturn("/test");
        when(request.header("Content-Type")).thenReturn(Optional.of(MediaType.JSON.name()));
        when(request.method()).thenReturn(HttpMethod.GET);
        
        // ends up in a NullPointerE
        handler.handle(request, mock(ServerResponse.class));
    }
}
