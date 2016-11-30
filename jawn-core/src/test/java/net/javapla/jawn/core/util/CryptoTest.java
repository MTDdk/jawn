package net.javapla.jawn.core.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import net.javapla.jawn.core.crypto.Crypto;
import net.javapla.jawn.core.crypto.HmacSha256Crypto;

public class CryptoTest {
    
    private static Crypto crypto;
    
    @Before
    public void setup() {
        crypto = new HmacSha256Crypto();
    }


    @Test
    public void hashing() {
        String key = "SomeRandomLongString";
        
        assertEquals("1432f1a1e8431c60a7117f831dc5d5c5e5cb00f15105558791ab9f1c3fdbb87a",
                crypto.hmac().sign("But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain", key));
        assertEquals("41c32e6e9c69286263a2c7ef42509dc3248c40186172608a6034e9d45658cdba",
                crypto.hmac().sign("was born and I will give you a complete account of the system",key));
        assertEquals("c8a5013890bccc204086e3923a26cb483f4ac8b31f564067854ef70714992606",
                crypto.hmac().sign("and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness",key));
    }

}
