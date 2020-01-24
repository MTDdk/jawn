package net.javapla.jawn.core;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

public interface Crypto {
    
    Signers hash();
    Encrypters encrypt();
    
    public static interface Signers {
        Signer SHA256();
    }
    
    public static interface Encrypters {
        Encrypter AES();
    }
    
    public static interface Signer {
        String sign(String value);
        String sign(String value, String key);
        int outputLength();
    }
    public static interface Encrypter {
        String encrypt(String data);
        String decrypt(String data);
    }
    
    public static abstract class SecretGenerator {
        
        private static final Random RND;
        static {
            Random r;
            try {
                r = SecureRandom.getInstance("SHA1PRNG");
            } catch (NoSuchAlgorithmException e) {
                r = new SecureRandom();
            }
            RND = r;
        }
        
        private static final int DEFAULT_SIZE = 33;
        
        public static String generate() {
            return generate(DEFAULT_SIZE);
        }
        
        public static String generate(final int lengthOfSecret) {
            byte[] bytes = new byte[lengthOfSecret];
            RND.nextBytes(bytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }
    }
    
}
