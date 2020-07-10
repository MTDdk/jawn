package net.javapla.jawn.server.undertow;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.xnio.ChannelListener;
import org.xnio.ChannelListener.Setter;

import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.server.WebSocket;
import net.javapla.jawn.core.server.WebSocketMessage;
import net.javapla.jawn.core.server.WebSocket.OnConnect;

public class UntertowWebSocketTest {
    
    private Setter<WebSocketChannel> setter;
    private WebSocketChannel channel;

    @SuppressWarnings("unchecked")
    @Before
    public void before() {
        setter = mock(ChannelListener.Setter.class);
        channel = mock(WebSocketChannel.class);
        when(channel.getReceiveSetter()).thenReturn(setter);
        
    }
    
    
    private static Config config;
    
    @BeforeClass
    public static void beforeClass() {
        config = mock(Config.class);
        when(config.get("server.ws.idle_timeout")).thenReturn("1m");
        when(config.getDuration(anyString(), any(TimeUnit.class))).thenCallRealMethod();
    }

    @Test
    public void fireConnect() {
        
        // setup
        OnConnect onConnect = mock(WebSocket.OnConnect.class);
        
        
        // execute
        UndertowWebSocket ws = new UndertowWebSocket(config, mock(UndertowRequest.class), channel);
        ws.onConnect(onConnect);
        ws.fireConnect();
        
        
        // assert
        verify(channel, times(1)).setIdleTimeout(anyLong());
        verify(channel, times(1)).getReceiveSetter();
        verify(channel, times(1)).resumeReceives();
        verify(setter, times(1)).set(ws);
        verify(onConnect, times(1)).onConnect(ws);
    }

    @Test
    public void onFullTextMessage() throws IOException {
        String data = "{ \"person\":{\"name\":\"cookie monster\"} }";
        
        // setup
        BufferedTextMessage text = mock(BufferedTextMessage.class);
        when(text.getData()).thenReturn(data);
        
        WebSocket.OnMessage message = mock(WebSocket.OnMessage.class);
        
        
        // execute
        UndertowWebSocket ws = new UndertowWebSocket(config, mock(UndertowRequest.class), channel);
        ws.onMessage(message);
        ws.fireConnect();
        ws.onFullTextMessage(channel, text);
        
        
        // assert
        ArgumentCaptor<WebSocketMessage> captor = ArgumentCaptor.forClass(WebSocketMessage.class);
        verify(message, times(1)).onMessage(any(UndertowWebSocket.class), captor.capture());
        assertThat(captor.getValue().value()).isEqualTo(data);
    }
}
