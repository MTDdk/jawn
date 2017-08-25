package net.javapla.jawn.core.crypto;

import com.google.inject.ImplementedBy;

@ImplementedBy(CryptoImpl.class)
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
        String sign(String value, String key);
    }
    public static interface Encrypter {
        String encrypt(String data);
        String decrypt(String data);
    }
}
