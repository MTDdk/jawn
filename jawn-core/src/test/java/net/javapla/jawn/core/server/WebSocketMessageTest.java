package net.javapla.jawn.core.server;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;

import org.junit.Test;

public class WebSocketMessageTest {

    @Test
    public void string() throws IOException {
        assertThat(WebSocketMessage.create("It worked!").value()).isEqualTo("It worked!");
        
        WebSocketMessage message = WebSocketMessage.create("73");
        assertThat(message.isPresent()).isTrue();
        assertThat(message.inMemory()).isTrue();
        assertThat(message.asInt()).isEqualTo(73);
        assertThat(message.bytes()).isNotEmpty();
        assertThat(message.size()).isEqualTo(2);
        assertThat(message.stream().available()).isEqualTo(2);
    }

    @Test
    public void byteArray() throws IOException {
        byte[] arr = {115, 111, 109, 101, 116, 104, 105, 110, 103, 32, 117, 115, 101, 102, 117, 108, 108};
        assertThat(WebSocketMessage.create(arr).value()).isEqualTo("something usefull");

        // "73"
        WebSocketMessage message = WebSocketMessage.create(new byte[]{55, 51});
        assertThat(message.asInt()).isEqualTo(73);
        assertThat(message.isPresent()).isTrue();
        assertThat(message.inMemory()).isTrue();
        assertThat(message.asInt()).isEqualTo(73);
        assertThat(message.bytes()).isNotEmpty();
        assertThat(message.size()).isEqualTo(2);
        assertThat(message.stream().available()).isEqualTo(2);
    }
}
