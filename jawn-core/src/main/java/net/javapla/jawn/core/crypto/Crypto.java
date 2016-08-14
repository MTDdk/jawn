package net.javapla.jawn.core.crypto;

public interface Crypto {

    Signer hmac();
    
    public static interface Signer {
        String sign(String value, String key);
    }
}
