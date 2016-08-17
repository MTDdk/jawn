package net.javapla.jawn.core.crypto;

import com.google.inject.ImplementedBy;

@ImplementedBy(HmacSha1Crypto.class)
public interface Crypto {

    Signer hmac();
    Encrypter encrypter();
    
    public static interface Signer {
        String sign(String value, String key);
    }
    public static interface Encrypter {
        String encrypt(String data);
        String decrypt(String data);
    }
}
