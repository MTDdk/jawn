package net.javapla.jawn.core;


import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class SecretGeneratorTest {

    @Test
    public void generate() {
        String generate = Crypto.SecretGenerator.generate(33);
        String standard = Crypto.SecretGenerator.generate();
        
        assertThat(generate.length()).isEqualTo(standard.length());
        assertThat(generate).isNotEqualTo(standard);
    }

}
