package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import java.time.Duration;

import org.junit.Test;

public class SessionConfigTest {

    @Test
    public void test() {
        SessionConfig.Impl impl = new SessionConfig.Impl();
        
        assertThat(impl.sessionStore).isNotNull();
        
        impl.memory();
        assertThat(impl.sessionStore).isNotNull();
        
        impl.memory(Duration.ofMinutes(17));
        assertThat(impl.sessionStore).isNotNull();
        
        impl.signed("secretkey");
        assertThat(impl.sessionStore).isNotNull();
        
        impl.store(SessionStore.signed("secretkey2", Cookie.builder("cook", "ie").build()));
        assertThat(impl.sessionStore).isNotNull();
    }

}
