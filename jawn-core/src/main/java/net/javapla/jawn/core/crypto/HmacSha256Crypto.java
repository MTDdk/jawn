package net.javapla.jawn.core.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.inject.Singleton;

@Singleton
public class HmacSha256Crypto implements Crypto {
    
    private final Signer HMAC_SHA256;
    private final Encrypter AES;
    
    public HmacSha256Crypto() {
        HMAC_SHA256 = new HmacSHA256();
        AES = new AesEncryption();
    }
    
    @Override
    public Signer hmac() {
        return HMAC_SHA256;
    }
    
    @Override
    public Encrypter encrypter() {
        return AES;
    }

    private static class HmacSHA256 implements Signer {
        public HmacSHA256() { }
        
        @Override
        public String sign(String value, String key) {
            try {
                // Get an hmac_sha1 key from the raw key bytes
                byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                String algorithm = "HmacSHA256";
                SecretKeySpec signingKey = new SecretKeySpec(keyBytes, algorithm);

                // Get an hmac_sha256 Mac instance and initialize with the signing key
                Mac mac = Mac.getInstance(algorithm);
                mac.init(signingKey);

                // Compute the hmac on input data bytes
                byte[] rawHmac = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));

                // Convert raw bytes to Hex
                return new String(printHexBinary(rawHmac));
                
            } catch (IllegalStateException | NoSuchAlgorithmException | InvalidKeyException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        
        private final char[] hexCode = "0123456789abcdef".toCharArray();
        private String printHexBinary(byte[] data) {
            char[] r = new char[data.length << 1];
            int index = 0;
            for (byte b : data) {
                r[index++] = hexCode[(b >> 4) & 0xF];
                r[index++] = hexCode[(b & 0xF)];
            }
            return new String(r);
        }
    }
    
    private static class AesEncryption implements Encrypter {
        static final String ALGORITHM = "AES";
        private final String applicationSecret;
        
        private final SecretKeySpec secretKeySpec;
        
        public AesEncryption() {
            //TODO read as property
            applicationSecret = "gawdDamnSecretThisIs!";
            
            try {
                int maxKeyLengthBits = Cipher.getMaxAllowedKeyLength(ALGORITHM);
                if (maxKeyLengthBits == Integer.MAX_VALUE) {
                    maxKeyLengthBits = 256;
                }

                secretKeySpec = new SecretKeySpec(applicationSecret.getBytes(StandardCharsets.UTF_8), 0, maxKeyLengthBits / Byte.SIZE, ALGORITHM);
                
                //logger.info("Session encryption is using {} / {} bit.", secretKeySpec.get().getAlgorithm(), maxKeyLengthBits);

            } catch (Exception exception) {
                //logger.error("Can not create class to encrypt.", exception);
                throw new RuntimeException(exception);
            }
        }


        @Override
        public String encrypt(String data) {
            Objects.requireNonNull(data, "Data to be encrypted");

            try {
                // encrypt data
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
                byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

                // convert encrypted bytes to string in base64
                return Base64.getEncoder().encodeToString(encrypted);

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
            byte[] decoded = Base64.getDecoder().decode(data);
            try {
                // decrypt bytes
                Cipher cipher = Cipher.getInstance(ALGORITHM);
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
    }
}
