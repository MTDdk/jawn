package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.google.inject.Injector;

import net.javapla.jawn.core.DeploymentInfo;
import net.javapla.jawn.core.Value;
import net.javapla.jawn.core.server.ServerRequest;
import net.javapla.jawn.core.server.ServerResponse;
import net.javapla.jawn.core.util.MultiList;

public class ContextImplTest {

    @Test
    public void emptyParam() {
        ServerRequest request = mock(ServerRequest.class);
        when(request.queryParam(anyString())).thenCallRealMethod();
        when(request.queryParams()).thenReturn(MultiList.empty());
        
        ContextImpl context = new ContextImpl(request, mock(ServerResponse.class), StandardCharsets.UTF_8, mock(DeploymentInfo.class), mock(Injector.class));
        
        Value value = context.param("date");
        assertThat(value.isPresent()).isFalse();
    }
    
    @Test
    public void attributes() {
        ContextImpl context = new ContextImpl(mock(ServerRequest.class), mock(ServerResponse.class), StandardCharsets.UTF_8, mock(DeploymentInfo.class), mock(Injector.class));
        
        context.attribute("at", "14");
        
        assertThat(context.attribute("at").get()).isEqualTo("14");
        assertThat(context.attribute("nothing").isPresent()).isFalse();
        assertThat(context.attribute("at", String.class).get()).isEqualTo("14");
        
        assertThrows(ClassCastException.class, () -> context.attribute("at", Double.class));
    }

}
