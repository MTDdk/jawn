package net.javapla.jawn.core.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.util.Constants;
import net.javapla.jawn.core.util.Modes;

public abstract class SecretGenerator {
    private static final Logger logger = LoggerFactory.getLogger(SecretGenerator.class);
    
    public static final int AES_SECRET_LENGTH = 64;
    
    public static String generate() {
        try {
            return generate(SecureRandom.getInstance("SHA1PRNG"), AES_SECRET_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            return generate(new SecureRandom(), AES_SECRET_LENGTH);
        }
    }

    protected static String generate(Random random, final int length) {
        char[] chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

        char[] result = new char[length];

        for (int i = 0; i < length; i++) {
            int pick = random.nextInt(chars.length);            
            result[i] = chars[pick];
        }

        return new String(result);
    }
    
    public static void check(JawnConfigurations properties) {
        if (properties.getMode() == Modes.PROD) return;
        
        Optional<String> secure = properties.getSecure(Constants.PROPERTY_SECURITY_SECRET);
        if (!secure.isPresent()) {
            logger.info("Jawn is generating a secret for you at " + Constants.PROPERTIES_FILE_USER);
            
            String secret = SecretGenerator.generate();
            properties.set(Constants.PROPERTY_SECURITY_SECRET, secret);
            
            // try to save it
            
        }
    }
}
