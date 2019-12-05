package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;

import net.javapla.jawn.core.Context.Request;

public class ContextRequestTest {

    @Test
    public void fullPath_shouldBe_sameAsPath() {
        Request request = mock(Context.Request.class);
        when(request.fullPath()).thenCallRealMethod();
        when(request.queryString()).thenReturn(Optional.empty());
        when(request.path()).thenReturn("/test");
        
        assertThat(request.fullPath()).isEqualTo("/test");
    }

    
    @Test
    public void fullPath_with_queryString() {
        Request request = mock(Context.Request.class);
        when(request.fullPath()).thenCallRealMethod();
        when(request.queryString()).thenReturn(Optional.of("read=true"));
        when(request.path()).thenReturn("/test");
        
        assertThat(request.fullPath()).isEqualTo("/test?read=true");
    }

}
