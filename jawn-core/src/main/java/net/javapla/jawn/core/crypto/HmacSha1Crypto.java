package net.javapla.jawn.core.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacSha1Crypto implements Crypto {
    
    public final Signer HMAC_SHA1;
    
    public HmacSha1Crypto() {
        HMAC_SHA1 = new HmacSHA1();
    }
    
    @Override
    public Signer hmac() {
        return HMAC_SHA1;
    }

    private static class HmacSHA1 implements Signer {
        public HmacSHA1() { }
        
        @Override
        public String sign(String value, String key) {
            try {
                // Get an hmac_sha1 key from the raw key bytes
                byte[] keyBytes = key.getBytes();
                String algorithm = "HmacSHA1";
                SecretKeySpec signingKey = new SecretKeySpec(keyBytes, algorithm);

                // Get an hmac_sha1 Mac instance and initialize with the signing key
                Mac mac = Mac.getInstance(algorithm);
                mac.init(signingKey);

                // Compute the hmac on input data bytes
                byte[] rawHmac = mac.doFinal(value.getBytes());

                // Convert raw bytes to Hex
                return new String(printHexBinary(rawHmac));
                
            } catch (IllegalStateException | NoSuchAlgorithmException | InvalidKeyException e) {
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
}
