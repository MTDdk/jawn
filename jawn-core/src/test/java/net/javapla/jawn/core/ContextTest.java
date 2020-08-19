package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;

public class ContextTest {

    @Test
    public void session_empty() {
        Context context = mock(Context.class);
        when(context.session(anyString())).thenCallRealMethod();
        
        when(context.sessionOptionally()).thenReturn(Optional.empty());
        
        assertThat(context.session("id").isPresent()).isFalse();
    }
    
    @Test
    public void sessionValue() {
        Context context = mock(Context.class);
        when(context.session(anyString())).thenCallRealMethod();
        
        Session session = mock(Session.class);
        when(context.sessionOptionally()).thenReturn(Optional.of(session));
        when(session.get(anyString())).thenReturn(Value.of("somevalue"));
        
        assertThat(context.session("id").isPresent()).isTrue();
    }

}
