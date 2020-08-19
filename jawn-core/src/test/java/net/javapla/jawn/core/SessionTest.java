package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;

import org.junit.Test;

public class SessionTest {

    @Test
    public void put() {
        Session session = mock(Session.class);
        when(session.put(anyString(), any(double.class))).thenCallRealMethod();
        when(session.put(anyString(), any(int.class))).thenCallRealMethod();
        when(session.put(anyString(), any(long.class))).thenCallRealMethod();
        when(session.put(anyString(), any(float.class))).thenCallRealMethod();
        when(session.put(anyString(), any(boolean.class))).thenCallRealMethod();
        
        session.put("bool", true);
        verify(session).put(anyString(), anyString());
        
        session.put("int", 13);
        verify(session, times(2)).put(anyString(), anyString());
        
        session.put("long", 11113l);
        verify(session, times(3)).put(anyString(), anyString());
        
        session.put("double", 13.1317);
        verify(session, times(4)).put(anyString(), anyString());
        
        session.put("float", 13.17f);
        verify(session, times(5)).put(anyString(), anyString());
    }

    @Test
    public void isExpired() {
        Session session = mock(Session.class);
        when(session.isExpired(any(Duration.class))).thenCallRealMethod();
        
        when(session.lastAccessed()).thenReturn(Instant.now().minusSeconds(20)); // twenty seconds into the past
        assertThat(session.isExpired(Duration.ofMinutes(1))).isFalse();
        
        when(session.lastAccessed()).thenReturn(Instant.now().minusSeconds(70)); // a minute and ten seconds into the past
        assertThat(session.isExpired(Duration.ofMinutes(1))).isTrue();
    }
}
