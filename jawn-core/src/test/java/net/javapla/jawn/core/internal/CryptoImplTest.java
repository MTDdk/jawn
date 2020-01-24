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
    

    @Test
    public void hashing_with_secret() {
        Config conf = mock(Config.class);
        when(conf.getOptionally(Constants.PROPERTY_SECURITY_SECRET)).thenReturn(Optional.of("SomeRandomLongString"));
        Crypto crypto = new CryptoImpl(conf);
        
        assertThat(crypto.hash().SHA256().sign("But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain"))
            .isEqualTo("1432f1a1e8431c60a7117f831dc5d5c5e5cb00f15105558791ab9f1c3fdbb87a");
        assertThat(crypto.hash().SHA256().sign("was born and I will give you a complete account of the system"))
            .isEqualTo("41c32e6e9c69286263a2c7ef42509dc3248c40186172608a6034e9d45658cdba");
        assertThat(crypto.hash().SHA256().sign("and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness"))
            .isEqualTo("c8a5013890bccc204086e3923a26cb483f4ac8b31f564067854ef70714992606");
    }
    
    @Test
    public void hashing_missing_secret() {
        Crypto crypto = new CryptoImpl(mock(Config.class));
        
        String key = "SomeRandomLongString";
        
        assertThat(crypto.hash().SHA256().sign("But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain", key))
            .isEqualTo("1432f1a1e8431c60a7117f831dc5d5c5e5cb00f15105558791ab9f1c3fdbb87a");
                
        assertThat(crypto.hash().SHA256().sign("was born and I will give you a complete account of the system",key))
            .isEqualTo("41c32e6e9c69286263a2c7ef42509dc3248c40186172608a6034e9d45658cdba");
                
        assertThat(crypto.hash().SHA256().sign("and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness",key))
            .isEqualTo("c8a5013890bccc204086e3923a26cb483f4ac8b31f564067854ef70714992606");
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

}
