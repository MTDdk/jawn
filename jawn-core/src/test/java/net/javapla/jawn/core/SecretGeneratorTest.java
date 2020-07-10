package net.javapla.jawn.core;


import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class SecretGeneratorTest {

    @Test
    public void generate() {
        byte[] generate = Crypto.SecretGenerator.generate(32);
        byte[] standard = Crypto.SecretGenerator.generate();
        
        assertThat(generate.length).isEqualTo(standard.length);
        assertThat(generate).isNotEqualTo(standard);
    }
    
    @Test
    public void encode() {
        String generate = Crypto.SecretGenerator.generateAndEncode(32);
        String standard = Crypto.SecretGenerator.generateAndEncode();
        System.out.println(generate);
        
        assertThat(generate.length()).isEqualTo(standard.length());
        assertThat(generate).isNotEqualTo(standard);
    }

}
