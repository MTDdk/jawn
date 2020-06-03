package net.javapla.jawn.core.server;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;

import org.junit.Test;

public class ServerTest {

    @Test
    public void test() {
        assertThat(Server.connectionResetByPeer(new IOException("connection reset by peer"))).isTrue();
        assertThat(Server.connectionResetByPeer(new IOException("connection reset by peer and some more"))).isTrue();
        assertThat(Server.connectionResetByPeer(new IOException("this particular connection reset by peer and some more"))).isTrue();
    }
    
    @Test
    public void missing() {
        assertThat(Server.connectionResetByPeer(null)).isFalse();
    }
    
    @Test
    public void notIOE() {
        assertThat(Server.connectionResetByPeer(new RuntimeException("testing"))).isFalse();
    }

}
