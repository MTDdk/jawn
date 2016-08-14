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
        assertEquals("f0f591a35650937c9559ee8f98cc29dac46c3fcb",
                crypto.hmac().sign("Sentence to sign", "Fxu6U5BTGIJZ06c8bD1xkhHc3Ct5JZXlst8tJ1K5uJJPaLdceDo6CUz0iWpjjQUY"));
        assertEquals("ba864c24a2a80a639d4f76bb44fd71650dcd4904",
                crypto.hmac().sign("Another sentence to sign","Fxu6U5BTGIJZ06c8bD1xkhHc3Ct5JZXlst8tJ1K5uJJPaLdceDo6CUz0iWpjjQUY"));
        assertEquals("4ad5fb0895dbc0c7172f9fc85d59f74b69f99b8b",
                crypto.hmac().sign("Yet another sentence to sign","Fxu6U5BTGIJZ06c8bD1xkhHc3Ct5JZXlst8tJ1K5uJJPaLdceDo6CUz0iWpjjQUY"));
    }

}
