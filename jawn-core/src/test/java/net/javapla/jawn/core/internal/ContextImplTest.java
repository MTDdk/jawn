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
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.server.ServerResponse;

public class ContextImplTest {

    @Test
    public void emptyParam() {
        ContextImpl context = new ContextImpl(mock(ServerRequest.class), mock(ServerResponse.class), StandardCharsets.UTF_8, mock(DeploymentInfo.class), mock(Injector.class));
        
        Value value = context.param("date");
        assertThat(value.isPresent()).isFalse();
    }
    
    @Test
    public void contentType() {
        ServerRequest request = mock(ServerRequest.class);
        when(request.header("Content-Type")).thenReturn(Optional.of("text/html; charset=utf-16"));
        
        ContextImpl context = new ContextImpl(request, mock(ServerResponse.class), StandardCharsets.UTF_8, mock(DeploymentInfo.class), mock(Injector.class));
        
        assertThat(context.req().charset()).isEqualTo(StandardCharsets.UTF_16);
        assertThat(context.req().contentType().matches(MediaType.HTML)).isTrue();
    }

}
