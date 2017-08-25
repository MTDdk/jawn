package net.javapla.jawn.core.crypto;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

public class SecretGeneratorTest {
    
    @BeforeClass
    public static void beforeClass() {
        // just to satisfy the coverage tool
        new SecretGenerator() {
        };
    }

    @Test
    public void seedLength() {
        assertEquals(SecretGenerator.generate().length(), SecretGenerator.AES_SECRET_LENGTH);
    }

    @Test
    public void genration() {
        assertEquals(SecretGenerator.generate(new Random(1),64), "NAvZuGESoIJ7hbqOIsAV4iWta9qh1yp4iuhRxkraBq7ZFYeOIN8pKbyLI3gOYbIv");
        assertEquals(SecretGenerator.generate(new Random(1),8), "NAvZuGES");
        assertEquals(SecretGenerator.generate(new Random(2), SecretGenerator.AES_SECRET_LENGTH), "oC8rHI6rDAiYSgMKHP6b4NlWG8UDdo5ALy66t3h2A5mhwWIBGjdyeFDBCoUn8Cov");
        assertEquals(SecretGenerator.generate(new Random(73), SecretGenerator.AES_SECRET_LENGTH), "nWc3Nw6WCQLqvykAVVhT0395cNnsfx7p60a4mSnXVt8iyru0Oz8uPCGYIqyLmUI6");
    }

}
