package net.javapla.jawn.core.http;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CookieTest {


    @Test
    public void builder_should_work() {
        String name = "cookiename";
        String value = "cookievalue";
        String comment = "comment";
        
        Cookie cookie = Cookie
            .builder(name, value)
            .setComment(comment)
            .setDomain("domain")
            .build();
        
        assertEquals(cookie.getComment(), comment);
        assertEquals(cookie.getName(), name);
        assertEquals(cookie.getValue(), value);
        assertEquals(cookie.getPath(), "/");
    }

}
