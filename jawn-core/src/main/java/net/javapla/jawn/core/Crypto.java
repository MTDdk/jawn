package net.javapla.jawn.core;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public interface Crypto {
    
    interface Signer {
        public static final String HMAC_SHA256 = "HmacSHA256";
        
        String sign(String value);
        //String sign(String value, String key);
        int outputLength();
        
        static Signer SHA256(String secret) {
            Mac mac;
            
            try {
                // Get an hmac_sha256 Mac instance
                mac = Mac.getInstance(HMAC_SHA256);
                
                // Get an hmac_sha256 key from the raw key bytes
                byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
                SecretKeySpec signingKey = new SecretKeySpec(keyBytes, HMAC_SHA256);
                
                // Initialise with the signing key
                mac.init(signingKey);
                
            } catch (InvalidKeyException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
            
            return new Signer() {

                @Override
                public String sign(String value) {
                    try {
                        // Compute the hmac on input data bytes
                        byte[] rawHmac = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    
                        // Convert raw bytes to base64
                        return new String(Base64.getEncoder().withoutPadding().encode(rawHmac), StandardCharsets.UTF_8); // Using base64 for fewer characters transferred than with hex
                    } catch (IllegalStateException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public int outputLength() {
                    // Base64.Encoder#outLength
                    int srclen = mac.getMacLength();
                    int n = srclen % 3;
                    return 4 * (srclen / 3) + (n == 0 ? 0 : n + 1);
                }
            };
        }
    }

    
    public static abstract class SecretGenerator {
        private static final Random RND = new SecureRandom();
        // MTD: might not be sufficiently safe. 
        // The default algorithm will try to obtain numbers from the underlying native OS, which will probably be better in most cases.
        /*static {
            Random r;
            try {
                r = SecureRandom.getInstance("SHA1PRNG");
            } catch (NoSuchAlgorithmException e) {
                r = new SecureRandom();
            }
            RND = r;
        }*/
        
        public static final int DEFAULT_SIZE = 32;
        
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
            return Base64.getUrlEncoder().withoutPadding().encodeToString(generate(lengthOfSecret));
        }
    }
}
