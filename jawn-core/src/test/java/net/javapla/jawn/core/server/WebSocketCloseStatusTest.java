package net.javapla.jawn.core.server;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class WebSocketCloseStatusTest {

    @Test
    public void valueOf_knownStatuses() {
        WebSocketCloseStatus stat = WebSocketCloseStatus.valueOf(1000).get();
        assertThat(stat).isEqualTo(WebSocketCloseStatus.NORMAL);
        
        stat = WebSocketCloseStatus.valueOf(1013).get();
        assertThat(stat).isEqualTo(WebSocketCloseStatus.SERVICE_OVERLOAD);
    }
    
    @Test
    public void valueOf_unknownStatus() {
        assertThat(WebSocketCloseStatus.valueOf(73).isPresent()).isFalse();
        assertThat(WebSocketCloseStatus.valueOf(0).isPresent()).isFalse();
        assertThat(WebSocketCloseStatus.valueOf(3000).isPresent()).isFalse();
    }

    @Test
    public void toString_() {
        assertThat(WebSocketCloseStatus.BAD_DATA.toString()).isEqualTo("1007(Bad data)");
        assertThat(new WebSocketCloseStatus(13, null).toString()).isEqualTo("13");
    }
    
    @Test
    public void code() {
        assertThat(WebSocketCloseStatus.BAD_DATA.code()).isEqualTo(1007);
        assertThat(new WebSocketCloseStatus(13, null).code()).isEqualTo(13);
    }
    
    @Test
    public void reason() {
        assertThat(WebSocketCloseStatus.BAD_DATA.reason()).isEqualTo("Bad data");
        assertThat(new WebSocketCloseStatus(13, null).reason()).isNull();
    }
}
