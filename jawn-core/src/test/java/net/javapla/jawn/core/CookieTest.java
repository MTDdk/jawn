package net.javapla.jawn.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class CookieTest {

    @Test
    void encodeNameAndValue() {
        Cookie cookie = new Cookie("jawncookie", "value");
        assertEquals("jawncookie=value;Path=/", cookie.toString());
    }

    @Test
    void escapeQuote() {
        Cookie cookie = new Cookie("jawncookie", "ab\"c");
        assertEquals("jawncookie=\"ab\\\"c\";Path=/", cookie.toString());
    }
    
    @Test
    void startQuote() {
        Cookie cookie = new Cookie("jawncookie", "\"abc");
        assertEquals("jawncookie=\"\\\"abc\";Path=/", cookie.toString());
    }
    
    @Test
    void badChar1() {
        assertThrows(IllegalArgumentException.class, () -> new Cookie("jawncookie", "\n").toString());
    }
    
    @Test
    void badChar2() {
        assertThrows(IllegalArgumentException.class, () -> new Cookie("jawncookie", "" + ((char)0x7f)).toString());
    }
    
    @Test
    void session() {
        Cookie cookie = new Cookie("jawncookie", "value").maxAge(-1);
        assertEquals("jawncookie=value;Path=/", cookie.toString());
    }
    
    @Test
    void emptyValue() {
        Cookie cookie = new Cookie("jawncookie", "");
        assertEquals("jawncookie=;Path=/", cookie.toString());
    }
    
    @Test
    void httpOnly() {
        Cookie cookie = new Cookie("jawncookie", "value").httpOnly(true);
        assertTrue(cookie.httpOnly());
        assertEquals("jawncookie=value;Path=/;HttpOnly", cookie.toString());
    }
    
    @Test
    void secure() {
        Cookie cookie = new Cookie("jawncookie", "value").secure(true);
        assertTrue(cookie.secure());
        assertEquals("jawncookie=value;Path=/;Secure", cookie.toString());
    }
    
    @Test
    void domain() {
        Cookie cookie = new Cookie("jawncookie", "value").domain(".javapla.net");
        assertEquals(cookie.domain(), ".javapla.net");
        assertEquals("jawncookie=value;Path=/;Domain=.javapla.net", cookie.toString());
    }
    
    @Test
    void maxAgeEpoch() {
        Cookie cookie = new Cookie("jawncookie", "value").maxAge(0);
        assertEquals(cookie.maxAge(), 0);
        assertEquals("jawncookie=value;Path=/;Max-Age=0;Expires=Thu, 01 Jan 1970 00:00:00 GMT", cookie.toString());
    }
    
    @Test
    void maxAgeOneDay() {
        Cookie cookie = new Cookie("jawncookie", "value").maxAge(Cookie.ONE_DAY);
        assertTrue(cookie.toString().startsWith("jawncookie=value;Path=/;Max-Age=86400;Expires="));
    }
    
    @Test
    void nullChecksName() {
        assertThrows(IllegalArgumentException.class, () -> new Cookie(null, "value"));
    }
    
    @Disabled("null value is used by sessions")
    @Test
    void nullChecksValue() {
        assertThrows(IllegalArgumentException.class, () -> new Cookie("name", null));
    }
    
    @Test
    void path() {
        Cookie cookie = new Cookie("name", "value");
        assertEquals("/", cookie.path());
        
        cookie = new Cookie(cookie).path("/test");
        assertEquals("/test", cookie.path());
    }
}
