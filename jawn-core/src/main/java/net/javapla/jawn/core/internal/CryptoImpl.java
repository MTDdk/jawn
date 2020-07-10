package net.javapla.jawn.core.internal;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.Crypto;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.StringUtil;

@Singleton
class CryptoImpl implements Crypto {
    private static final Logger logger = LoggerFactory.getLogger(Crypto.class);
    
    private final Signers signers;
    private final Encrypters encrypters;
    
    private final Signer HMAC_SHA256;
    private final Encrypter AES;
    
    @Inject
    public CryptoImpl(Config config) {
        Optional<String> secret = config.getOptionally(Constants.PROPERTY_SECURITY_SECRET);
        if (!secret.isPresent()) {
            logger.error("The key {} does not exist and encryption will not work properly. Please include it in your {}.", Constants.PROPERTY_SECURITY_SECRET, Constants.PROPERTIES_FILE_USER);
        }
        
        HMAC_SHA256 = new HmacSHA256(secret);
        AES = new AesEncryption(secret);
        
        signers = new Signers() {
            @Override
            public Signer SHA256() {
                return HMAC_SHA256;
            }
        };
        
        encrypters = new Encrypters() {
            @Override
            public Encrypter AES() {
                return AES;
            }
        };
    }
    
    @Override
    public Signers hash() {
        return signers;
    }
    
    @Override
    public Encrypters encrypt() {
        return encrypters;
    }

    private static class HmacSHA256 implements Signer {
        static final String ALGORITHM = "HmacSHA256";
        private final Mac mac;
        
        private HmacSHA256(Optional<String> secret) {
            try {
                // Get an hmac_sha256 Mac instance and initialize with the signing key
                mac = Mac.getInstance(ALGORITHM);
                
                
                secret.ifPresent(key -> {
                    // Get an hmac_sha256 key from the raw key bytes
                    byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                    SecretKeySpec signingKey = new SecretKeySpec(keyBytes, ALGORITHM);
                    
                    try {
                        mac.init(signingKey);
                    } catch (InvalidKeyException e) {
                        throw new RuntimeException(e);
                    }
                });
                
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        
        
        @Override
        public String sign(String value) {
            try {
                // Compute the hmac on input data bytes
                byte[] rawHmac = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
                mac.reset();

                // Convert raw bytes to base64
                return Base64.getEncoder().withoutPadding().encodeToString(rawHmac);
                
            } catch (IllegalStateException e) {
                logger.error(getHelperLogMessage(), e);
                throw new IllegalArgumentException(e);
            }
        }
        
        @Override
        public String sign(String value, String key) {
            try {
                // Get an hmac_sha1 key from the raw key bytes
                byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
                SecretKeySpec signingKey = new SecretKeySpec(keyBytes, ALGORITHM);

                // Get an hmac_sha256 Mac instance and initialize with the signing key
                Mac mac = Mac.getInstance(ALGORITHM);
                mac.init(signingKey);

                // Compute the hmac on input data bytes
                byte[] rawHmac = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));

                // Convert raw bytes to base64
                return Base64.getEncoder().withoutPadding().encodeToString(rawHmac); // Using base64 for fewer characters transferred than with hex 
                
            } catch (IllegalStateException | InvalidKeyException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public int outputLength() {
            return 64;
        }
        
    }
    
    private static class AesEncryption implements Encrypter {
        //TODO
        // Use AES/CBC/PKCS7PADDING and create something that correctly uses and stores IVs
        //https://www.owasp.org/index.php/Using_the_Java_Cryptographic_Extensions
        //http://stackoverflow.com/questions/20796042/aes-encryption-and-decryption-with-java
        static final String ALGORITHM = "AES";
        static final int AES_SECRET_LENGTH = 64;
        
        private final int keyLength = AES_SECRET_LENGTH;
        private final Optional<SecretKeySpec> secretKeySpec;
        
        private AesEncryption(Optional<String> secret) {
            Optional<SecretKeySpec> keySpec = Optional.empty();
            
            if (secret.isPresent()) {
                String applicationSecret = secret.get();
                if (StringUtil.blank(applicationSecret)) {
                    throw new IllegalArgumentException(Constants.PROPERTY_SECURITY_SECRET + " is blank");
                }
                
                // TODO Perhaps some sanitisation of the secret - it seems '-' is illegal
                
                if (applicationSecret.length() < keyLength) {
                    applicationSecret = Crypto.expandSecret(applicationSecret, keyLength);
                }

                try {
                    int maxKeyLengthBits = Cipher.getMaxAllowedKeyLength(ALGORITHM);
                    if (maxKeyLengthBits == Integer.MAX_VALUE) {
                        maxKeyLengthBits = 256;
                    }

                    keySpec = Optional.of(new SecretKeySpec(applicationSecret.getBytes(StandardCharsets.UTF_8), 0, maxKeyLengthBits / Byte.SIZE, ALGORITHM));

                    logger.info("AES encryption is using {} / {} bit.", keySpec.get().getAlgorithm(), maxKeyLengthBits);

                } catch (Exception exception) {
                    logger.error("Can not create class to encrypt.", exception);
                    throw new RuntimeException(exception);
                }
            }
            
            secretKeySpec = keySpec;
        }


        @Override
        public String encrypt(String data) {
            Objects.requireNonNull(data, "Data to be encrypted");
            
            if (!secretKeySpec.isPresent()) return data;

            try {
                // encrypt data
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec.get());
                byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

                // convert encrypted bytes to string in base64
                return Base64.getEncoder()/*getUrlEncoder()*/.encodeToString(encrypted);

            } catch (InvalidKeyException ex) {
                logger.error(getHelperLogMessage(), ex);
                throw new RuntimeException(ex);
            } catch (GeneralSecurityException ex) {
                logger.error("Failed to encrypt data.", ex);
                throw new RuntimeException(ex);
            }
        }


        @Override
        public String decrypt(String data) {
            Objects.requireNonNull(data, "Data to be decrypted");

            if (!secretKeySpec.isPresent()) return data;

            // convert base64 encoded string to bytes
            byte[] decoded = Base64.getDecoder()/*getUrlDecoder()*/.decode(data);
            try {
                // decrypt bytes
                Cipher cipher = Cipher.getInstance(ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec.get());
                byte[] decrypted = cipher.doFinal(decoded);

                // convert bytes to string
                return new String(decrypted, StandardCharsets.UTF_8);

            } catch (InvalidKeyException ex) {
                logger.error(getHelperLogMessage(), ex);
                throw new RuntimeException(ex);
            } catch (GeneralSecurityException ex) {
                logger.error("Failed to decrypt data.", ex);
                throw new RuntimeException(ex);
            }
        }
        
        @Override
        public int keyLength() {
            return keyLength;
        }
    }
    
    private static String getHelperLogMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Invalid key provided. Check if application secret is properly set.").append(System.lineSeparator());
        sb.append("You can remove '").append(Constants.PROPERTY_SECURITY_SECRET).append("' key in configuration file ");
        sb.append("and restart application.");
        //sb.append("and restart application. We will generate new key for you.");
        return sb.toString();
    }
}
