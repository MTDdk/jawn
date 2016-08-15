package net.javapla.jawn.core.util;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import net.javapla.jawn.core.crypto.Crypto;
import net.javapla.jawn.core.crypto.HmacSha1Crypto;

public class CryptoTest {
    
    private Crypto crypto;
    
    @Before
    public void setup() {
        crypto = new HmacSha1Crypto();
    }


    @Test
    public void test() {
        String key = "SomeRandomLongString";
        
        assertEquals("017da2e74dc43aba138082680b6f5928512f0535",
                crypto.hmac().sign("But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain", key));
        assertEquals("2cdc4a0cddf55fcb4b5e004e4aa745a0977a90d6",
                crypto.hmac().sign("was born and I will give you a complete account of the system",key));
        assertEquals("a9b9ac4ab6e74f9e650c9985e798b2e5bb6766ac",
                crypto.hmac().sign("and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness",key));
    }

}
