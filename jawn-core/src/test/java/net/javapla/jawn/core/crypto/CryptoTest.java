package net.javapla.jawn.core.crypto;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.javapla.jawn.core.configuration.JawnConfigurations;
import net.javapla.jawn.core.crypto.Crypto;
import net.javapla.jawn.core.crypto.CryptoImpl;
import net.javapla.jawn.core.util.Constants;

public class CryptoTest {
    
//    private static Crypto crypto;
    private static JawnConfigurations props;
    
    @Before
    public void setup() {
        try {
            //JawnConfigurations props = new JawnConfigurations(Modes.TEST);
            props = mock(JawnConfigurations.class);
//            crypto = new CryptoImpl(props);
        } catch(Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void hashing_with_secret() {
        when(props.getSecure(Constants.PROPERTY_SECURITY_SECRET)).thenReturn(Optional.of("SomeRandomLongString"));
        Crypto crypto = new CryptoImpl(props);
        
        assertEquals("1432f1a1e8431c60a7117f831dc5d5c5e5cb00f15105558791ab9f1c3fdbb87a",
                crypto.hash().SHA256().sign("But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain"));
        assertEquals("41c32e6e9c69286263a2c7ef42509dc3248c40186172608a6034e9d45658cdba",
                crypto.hash().SHA256().sign("was born and I will give you a complete account of the system"));
        assertEquals("c8a5013890bccc204086e3923a26cb483f4ac8b31f564067854ef70714992606",
                crypto.hash().SHA256().sign("and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness"));
    }
    
    @Test
    public void hashing_missing_secret() {
        Crypto crypto = new CryptoImpl(props);
        
        String key = "SomeRandomLongString";
        
        assertEquals("1432f1a1e8431c60a7117f831dc5d5c5e5cb00f15105558791ab9f1c3fdbb87a",
                crypto.hash().SHA256().sign("But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain", key));
        assertEquals("41c32e6e9c69286263a2c7ef42509dc3248c40186172608a6034e9d45658cdba",
                crypto.hash().SHA256().sign("was born and I will give you a complete account of the system",key));
        assertEquals("c8a5013890bccc204086e3923a26cb483f4ac8b31f564067854ef70714992606",
                crypto.hash().SHA256().sign("and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness",key));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void hashing_missing_secretAndKey() {
        Crypto crypto = new CryptoImpl(props);
        
        crypto.hash().SHA256().sign("anything");
    }
    
    @Test
    public void encryption() {
        when(props.getSecure(Constants.PROPERTY_SECURITY_SECRET)).thenReturn(Optional.of("7NbiXhRxA0RYi7tbRC2vbU1Wu54VoBmjDqPxu0ZdCdgQgL31OOYttQ6dVkl8ZErm"));
        Crypto crypto = new CryptoImpl(props);
        
        String stringToEncrypt = "But I must explain";
        String encrypted = crypto.encrypt().AES().encrypt(stringToEncrypt);
        Assert.assertThat(encrypted, not(equalTo(stringToEncrypt)));
        
        String decrypted = crypto.encrypt().AES().decrypt(encrypted);
        Assert.assertThat(decrypted, equalTo(stringToEncrypt));
    }
    
    @Test
    public void emptyStringEncryption() {
        when(props.getSecure(Constants.PROPERTY_SECURITY_SECRET)).thenReturn(Optional.of("7NbiXhRxA0RYi7tbRC2vbU1Wu54VoBmjDqPxu0ZdCdgQgL31OOYttQ6dVkl8ZErm"));
        Crypto crypto = new CryptoImpl(props);
        
        String stringToEncrypt = "";
        String encrypted = crypto.encrypt().AES().encrypt(stringToEncrypt);
        Assert.assertThat(encrypted, not(equalTo(stringToEncrypt)));
        
        String decrypted = crypto.encrypt().AES().decrypt(encrypted);
        Assert.assertThat(decrypted, equalTo(stringToEncrypt));
    }

    @Test
    public void encryption_should_expandSecretWhenTooShort() {
        when(props.getSecure(Constants.PROPERTY_SECURITY_SECRET)).thenReturn(Optional.of("555"));
        Crypto crypto = new CryptoImpl(props);
        
        String stringToEncrypt = "But I must explain";
        String encrypted = crypto.encrypt().AES().encrypt(stringToEncrypt);
        Assert.assertThat(encrypted, not(equalTo(stringToEncrypt)));
        
        String decrypted = crypto.encrypt().AES().decrypt(encrypted);
        Assert.assertThat(decrypted, equalTo(stringToEncrypt));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void fail_when_secretIsBlank() {
        when(props.getSecure(Constants.PROPERTY_SECURITY_SECRET)).thenReturn(Optional.of(""));
        new CryptoImpl(props);
    }
    
    @Test
    public void encryptNothing_when_secretIsMissing() {
        Crypto crypto = new CryptoImpl(props);
        
        String stringToEncrypt = "But I must explain";
        String encrypted = crypto.encrypt().AES().encrypt(stringToEncrypt);
        Assert.assertThat(encrypted, equalTo(stringToEncrypt));
        
        String decrypted = crypto.encrypt().AES().decrypt(encrypted);
        Assert.assertThat(decrypted, equalTo(stringToEncrypt));
    }
}
