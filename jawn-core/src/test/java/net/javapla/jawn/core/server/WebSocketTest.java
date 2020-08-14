package net.javapla.jawn.core.server;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class WebSocketTest {

    @Test
    public void close() {
        WebSocket socket = mock(WebSocket.class);
        when(socket.close()).thenCallRealMethod();
        
        socket.close();
        
        verify(socket).close(eq(WebSocketCloseStatus.NORMAL));
    }

}
