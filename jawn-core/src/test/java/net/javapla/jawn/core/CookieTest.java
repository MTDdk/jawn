package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class CookieTest {

    @Test
    public void encodeNameAndValue() {
        Cookie cookie = Cookie.builder("jawncookie", "value").build();
        assertThat(cookie.toString()).isEqualTo("jawncookie=value;Version=1;Path=/");
    }

    @Test
    public void escapeQuote() {
        Cookie cookie = Cookie.builder("jawncookie", "ab\"c").build();
        assertThat(cookie.toString()).isEqualTo("jawncookie=\"ab\\\"c\";Version=1;Path=/");
    }
    
    @Test
    public void startQuote() {
        Cookie cookie = Cookie.builder("jawncookie", "\"abc").build();
        assertThat(cookie.toString()).isEqualTo("jawncookie=\"\\\"abc\";Version=1;Path=/");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void badChar1() {
        Cookie.builder("jawncookie", "\n").build().toString();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void badChar2() {
        Cookie.builder("jawncookie", "" + ((char)0x7f)).build().toString();
    }
    
    @Test
    public void session() {
        Cookie cookie = Cookie.builder("jawncookie", "value").maxAge(-1).build();
        assertThat(cookie.toString()).isEqualTo("jawncookie=value;Version=1;Path=/");
    }
    
    @Test
    public void emptyValue() {
        Cookie cookie = Cookie.builder("jawncookie", "").build();
        assertThat(cookie.toString()).isEqualTo("jawncookie=;Version=1;Path=/");
    }
    
    @Test
    public void httpOnly() {
        Cookie cookie = Cookie.builder("jawncookie", "value").httpOnly().build();
        assertThat(cookie.httpOnly()).isTrue();
        assertThat(cookie.toString()).isEqualTo("jawncookie=value;Version=1;Path=/;HttpOnly");
    }
    
    @Test
    public void secure() {
        Cookie cookie = Cookie.builder("jawncookie", "value").secure().build();
        assertThat(cookie.secure()).isTrue();
        assertThat(cookie.toString()).isEqualTo("jawncookie=value;Version=1;Path=/;Secure");
    }
    
    @Test
    public void domain() {
        Cookie cookie = Cookie.builder("jawncookie", "value").domain(".javapla.net").build();
        assertThat(cookie.domain()).isEqualTo(".javapla.net");
        assertThat(cookie.toString()).isEqualTo("jawncookie=value;Version=1;Path=/;Domain=.javapla.net");
    }
    
    @Test
    public void maxAgeEpoch() {
        Cookie cookie = Cookie.builder("jawncookie", "value").maxAge(0).build();
        assertThat(cookie.maxAge()).isEqualTo(0);;
        assertThat(cookie.toString()).isEqualTo("jawncookie=value;Version=1;Path=/;Max-Age=0;Expires=Thu, 01 Jan 1970 00:00:00 GMT");
    }
    
    @Test
    public void maxAgeOneDay() {
        Cookie cookie = Cookie.builder("jawncookie", "value").maxAge(Cookie.ONE_DAY).build();
        assertThat(cookie.toString().startsWith("jawncookie=value;Version=1;Path=/;Max-Age=86400;Expires=")).isTrue();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void nullChecksName() {
        Cookie.builder(null, "value");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void nullChecksValue() {
        Cookie.builder("name", null);
    }
    
    @Test
    public void builder() {
        Cookie cookie = Cookie.builder("name", "value").build();
        assertThat(cookie.path()).isEqualTo("/");
        
        cookie = Cookie.builder(cookie).path("/test").build();
        assertThat(cookie.path()).isEqualTo("/test");
    }
}
