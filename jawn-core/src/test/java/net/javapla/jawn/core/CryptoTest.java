package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class CryptoTest {

    @Test
    public void expandSecret() {
        String expanded = Crypto.expandSecret("test", 64);
        assertThat(expanded).hasLength(64);
        
        expanded = Crypto.expandSecret("test", 8);
        assertThat(expanded).hasLength(8);
        assertThat(expanded).isEqualTo("testtest");
        
        expanded = Crypto.expandSecret("testtest", 4);
        assertThat(expanded).hasLength(4);
        assertThat(expanded).isEqualTo("test");
    }


}
