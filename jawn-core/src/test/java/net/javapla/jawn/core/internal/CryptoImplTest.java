package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.Crypto;
import net.javapla.jawn.core.util.Constants;

public class CryptoImplTest {
    
    /*@Test
    public void hashing_with_secret() {
        Config conf = mock(Config.class);
        when(conf.getOptionally(Constants.PROPERTY_SECURITY_SECRET)).thenReturn(Optional.of("SomeRandomLongString"));
        Crypto crypto = new CryptoImpl(conf);
        
        assertThat(crypto.hash().SHA256().sign("But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain"))
            .isEqualTo("FDLxoehDHGCnEX+DHcXVxeXLAPFRBVWHkaufHD/buHo");
        assertThat(crypto.hash().SHA256().sign("was born and I will give you a complete account of the system"))
            .isEqualTo("QcMubpxpKGJjosfvQlCdwySMQBhhcmCKYDTp1FZYzbo");
        assertThat(crypto.hash().SHA256().sign("and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness"))
            .isEqualTo("yKUBOJC8zCBAhuOSOibLSD9KyLMfVkBnhU73BxSZJgY");
    }
    
    @Test
    public void hashing_missing_secret() {
        Crypto crypto = new CryptoImpl(mock(Config.class));
        
        String key = "SomeRandomLongString";
        
        assertThat(crypto.hash().SHA256().sign("But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain", key))
            .isEqualTo("FDLxoehDHGCnEX+DHcXVxeXLAPFRBVWHkaufHD/buHo");
                
        assertThat(crypto.hash().SHA256().sign("was born and I will give you a complete account of the system",key))
            .isEqualTo("QcMubpxpKGJjosfvQlCdwySMQBhhcmCKYDTp1FZYzbo");
                
        assertThat(crypto.hash().SHA256().sign("and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness",key))
            .isEqualTo("yKUBOJC8zCBAhuOSOibLSD9KyLMfVkBnhU73BxSZJgY");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void hashing_missing_secretAndKey() {
        Crypto crypto = new CryptoImpl(mock(Config.class));
        
        crypto.hash().SHA256().sign("anything");
    }
    
    @Test
    public void encryption() {
        Config conf = mock(Config.class);
        when(conf.getOptionally(Constants.PROPERTY_SECURITY_SECRET)).thenReturn(Optional.of("7NbiXhRxA0RYi7tbRC2vbU1Wu54VoBmjDqPxu0ZdCdgQgL31OOYttQ6dVkl8ZErm"));
        Crypto crypto = new CryptoImpl(conf);
        
        String stringToEncrypt = "But I must explain";
        String encrypted = crypto.encrypt().AES().encrypt(stringToEncrypt);
        assertThat(encrypted).isNotEqualTo(stringToEncrypt);
        
        String decrypted = crypto.encrypt().AES().decrypt(encrypted);
        assertThat(decrypted).isEqualTo(stringToEncrypt);
    }
    
    @Test
    public void emptyStringEncryption() {
        Config conf = mock(Config.class);
        when(conf.getOptionally(Constants.PROPERTY_SECURITY_SECRET)).thenReturn(Optional.of("7NbiXhRxA0RYi7tbRC2vbU1Wu54VoBmjDqPxu0ZdCdgQgL31OOYttQ6dVkl8ZErm"));
        Crypto crypto = new CryptoImpl(conf);
        
        String stringToEncrypt = "";
        String encrypted = crypto.encrypt().AES().encrypt(stringToEncrypt);
        assertThat(encrypted).isNotEqualTo(stringToEncrypt);
        
        String decrypted = crypto.encrypt().AES().decrypt(encrypted);
        assertThat(decrypted).isEqualTo(stringToEncrypt);
    }
    
    @Test
    public void encryption_should_expandSecretWhenTooShort() {
        Config conf = mock(Config.class);
        when(conf.getOptionally(Constants.PROPERTY_SECURITY_SECRET)).thenReturn(Optional.of("555"));
        Crypto crypto = new CryptoImpl(conf);
        
        String stringToEncrypt = "But I must explain";
        String encrypted = crypto.encrypt().AES().encrypt(stringToEncrypt);
        assertThat(encrypted).isNotEqualTo(stringToEncrypt);
        
        String decrypted = crypto.encrypt().AES().decrypt(encrypted);
        assertThat(decrypted).isEqualTo(stringToEncrypt);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void fail_when_secretIsBlank() {
        Config conf = mock(Config.class);
        when(conf.getOptionally(Constants.PROPERTY_SECURITY_SECRET)).thenReturn(Optional.of(""));
        new CryptoImpl(conf);
    }
    
    @Test
    public void encryptNothing_when_secretIsMissing() {
        Crypto crypto = new CryptoImpl(mock(Config.class));
        
        String stringToEncrypt = "But I must explain";
        String encrypted = crypto.encrypt().AES().encrypt(stringToEncrypt);
        assertThat(encrypted).isEqualTo(stringToEncrypt);
        
        String decrypted = crypto.encrypt().AES().decrypt(encrypted);
        assertThat(decrypted).isEqualTo(stringToEncrypt);
    }
    */
}
