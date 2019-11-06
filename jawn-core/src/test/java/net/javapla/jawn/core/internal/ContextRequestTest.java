package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.Test;

import com.google.inject.Injector;

import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.MediaType;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.server.ServerResponse;

public class ContextRequestTest {

    @Test
    public void contentType() {
        ServerRequest request = mock(ServerRequest.class);
        when(request.header("Content-Type")).thenReturn(Optional.of("text/html; charset=utf-16"));
        
        ContextImpl context = new ContextImpl(request, mock(ServerResponse.class), StandardCharsets.UTF_8, mock(DeploymentInfo.class), mock(Injector.class));
        
        assertThat(context.req().charset()).isEqualTo(StandardCharsets.UTF_16);
        assertThat(context.req().contentType().matches(MediaType.HTML)).isTrue();
    }
    
    @Test
    public void queryString() {
        ServerRequest request = mock(ServerRequest.class);
        when(request.queryString()).thenReturn("number888");
        
        ContextImpl context = new ContextImpl(request, mock(ServerResponse.class), StandardCharsets.UTF_8, mock(DeploymentInfo.class), mock(Injector.class));
        
        assertThat(context.req().queryString().get()).isEqualTo("number888");
    }
    
    @Test
    public void queryString_empty() {
        ServerRequest request = mock(ServerRequest.class);
        when(request.queryString()).thenReturn("");
        
        ContextImpl context = new ContextImpl(request, mock(ServerResponse.class), StandardCharsets.UTF_8, mock(DeploymentInfo.class), mock(Injector.class));
        
        assertThat(context.req().queryString().isPresent()).isFalse();
    }
}
