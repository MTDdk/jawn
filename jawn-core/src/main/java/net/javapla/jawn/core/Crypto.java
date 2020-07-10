package net.javapla.jawn.core;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

public interface Crypto {
    
    Signers hash();
    Encrypters encrypt();
    
    interface Signers {
        Signer SHA256();
    }
    
    interface Encrypters {
        Encrypter AES();
    }
    
    interface Signer {
        String sign(String value);
        String sign(String value, String key);
        int outputLength();
    }
    interface Encrypter {
        String encrypt(String data);
        String decrypt(String data);
        int keyLength();
    }
    
    static String expandSecret(String secret, int neededLength) {
        StringBuilder bob = new StringBuilder(neededLength);
        
        while (bob.length() < neededLength) {
            if (bob.length() + secret.length() < neededLength) {
                bob.append(secret);
            } else {
                bob.append(secret.substring(0, neededLength - bob.length()));
                break;
            }
        }
        
        return bob.toString();
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
        
        private static final int DEFAULT_SIZE = 32;
        
        public static byte[] generate() {
            return generate(DEFAULT_SIZE);
        }
        
        public static byte[] generate(final int lengthOfSecret) {
            byte[] bytes = new byte[lengthOfSecret];
            RND.nextBytes(bytes);
            return bytes;
        }
        
        public static String generateAndEncode() {
            return generateAndEncode(DEFAULT_SIZE);
        }
        
        public static String generateAndEncode(final int lengthOfSecret) {
            return Base64.getEncoder().withoutPadding().encodeToString(generate(lengthOfSecret));
        }
    }
    
}
