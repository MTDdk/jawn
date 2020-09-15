package net.javapla.jawn.core;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import net.javapla.jawn.core.util.StringUtil;


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
    
    
    interface Encrypter {
        String encrypt(String data);
        String decrypt(String data);
        int keyLength();
        
        
        static Encrypter AES(String secret) {
            if (StringUtil.blank(secret)) throw new IllegalArgumentException("Your secret key may not be empty");
            
            
            final String ALGORITHM = "AES";
            final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
            //final int AES_KEY_LENGTH_BITS = 128;
            final int secretLength = 16; // AES_KEY_LENGTH_BITS / Byte.SIZE
                    
            
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException ignore) { throw new RuntimeException(ignore); }
            
            final byte[] key = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            final SecretKeySpec secretKeySpec = new SecretKeySpec(key, 0, secretLength, ALGORITHM);

            //logger.info("AES encryption is using {} / {} bit.", keySpec.get().getAlgorithm(), maxKeyLengthBits);

            return new Encrypter() {

                @Override
                public String encrypt(String data) {
                    Objects.requireNonNull(data, "Data to be encrypted");
                    
                    try {
                        // encrypt data
                        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
                        byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
    
                        // convert encrypted bytes to string in base64
                        return new String(Base64.getEncoder().withoutPadding().encode(encrypted), StandardCharsets.UTF_8);
                        //return Base64.getEncoder()/*getUrlEncoder()*/.encodeToString(encrypted);
                    } catch (InvalidKeyException ex) {
                        //logger.error(getHelperLogMessage(), ex);
                        throw new RuntimeException(ex);
                    } catch (GeneralSecurityException ex) {
                        //logger.error("Failed to encrypt data.", ex);
                        throw new RuntimeException(ex);
                    }
                }
    
    
                @Override
                public String decrypt(String data) {
                    Objects.requireNonNull(data, "Data to be decrypted");
    
                    // convert base64 encoded string to bytes
                    byte[] decoded = Base64.getDecoder()/*getUrlDecoder()*/.decode(data);
                    try {
                        // decrypt bytes
                        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
                        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
                        byte[] decrypted = cipher.doFinal(decoded);
    
                        // convert bytes to string
                        return new String(decrypted, StandardCharsets.UTF_8);
    
                    } catch (InvalidKeyException ex) {
                        //logger.error(getHelperLogMessage(), ex);
                        throw new RuntimeException(ex);
                    } catch (GeneralSecurityException ex) {
                        //logger.error("Failed to decrypt data.", ex);
                        throw new RuntimeException(ex);
                    }
                }
                
                @Override
                public int keyLength() {
                    return secretLength;
                }
                
            };
        }
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
