package net.javapla.jawn.core.server;

import static com.google.common.truth.Truth.assertThat;

import java.io.IOException;

import org.junit.Test;

public class ThrowableCauseTest {

    @Test
    public void test() {
        assertThat(ThrowableCause.connectionResetByPeer(new IOException("connection reset by peer"))).isTrue();
        assertThat(ThrowableCause.connectionResetByPeer(new IOException("connection reset by peer and some more"))).isTrue();
        assertThat(ThrowableCause.connectionResetByPeer(new IOException("this particular connection reset by peer and some more"))).isTrue();
    }
    
    @Test
    public void missing() {
        assertThat(ThrowableCause.connectionResetByPeer(null)).isFalse();
    }
    
    @Test
    public void notIOE() {
        assertThat(ThrowableCause.connectionResetByPeer(new RuntimeException("testing"))).isFalse();
    }

}
