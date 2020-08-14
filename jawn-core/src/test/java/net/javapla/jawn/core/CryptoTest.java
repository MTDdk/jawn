package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class CryptoTest {
    
    @Test
    public void encryption() {
        Crypto.Encrypter aes = Crypto.Encrypter.AES("7NbiXhRxA0RYi7tbRC2vbU1Wu54VoBmjDqPxu0ZdCdgQgL31OOYttQ6dVkl8ZErm");
        
        String stringToEncrypt = "But I must explain";
        String encrypted = aes.encrypt(stringToEncrypt);
        assertThat(encrypted).isNotEqualTo(stringToEncrypt);
        
        String decrypted = aes.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(stringToEncrypt);
    }
    
    @Test
    public void emptyStringEncryption() {
        Crypto.Encrypter aes = Crypto.Encrypter.AES("7NbiXhRxA0RYi7tbRC2vbU1Wu54VoBmjDqPxu0ZdCdgQgL31OOYttQ6dVkl8ZErm");
        
        String stringToEncrypt = "";
        String encrypted = aes.encrypt(stringToEncrypt);
        assertThat(encrypted).isNotEqualTo(stringToEncrypt);
        
        String decrypted = aes.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(stringToEncrypt);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void fail_when_secretIsBlank() {
        Crypto.Encrypter.AES("");
    }
    
    @Test
    public void hashing_with_secret() {
        Crypto.Signer sha = Crypto.Signer.SHA256("SomeRandomLongString");

        assertThat(sha.sign("But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain"))
            .isEqualTo("FDLxoehDHGCnEX+DHcXVxeXLAPFRBVWHkaufHD/buHo");
        assertThat(sha.sign("was born and I will give you a complete account of the system"))
            .isEqualTo("QcMubpxpKGJjosfvQlCdwySMQBhhcmCKYDTp1FZYzbo");
        assertThat(sha.sign("and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness"))
            .isEqualTo("yKUBOJC8zCBAhuOSOibLSD9KyLMfVkBnhU73BxSZJgY");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void hashing_missing_secretAndKey() {
        Crypto.Signer.SHA256("").sign("anything");
    }

}
