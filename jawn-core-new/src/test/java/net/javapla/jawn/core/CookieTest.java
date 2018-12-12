package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;

public class CookieTest {

    @Test
    public void encodeNameAndValue() {
        Cookie cookie = new Cookie.Builder("jawncookie", "value").build();
        assertThat(cookie.toString()).isEqualTo("jawncookie=value;Version=1;Path=/");
    }

    @Test
    public void escapeQuote() {
        Cookie cookie = new Cookie.Builder("jawncookie", "ab\"c").build();
        assertThat(cookie.toString()).isEqualTo("jawncookie=\"ab\\\"c\";Version=1;Path=/");
    }
    
    @Test
    public void startQuote() {
        Cookie cookie = new Cookie.Builder("jawncookie", "\"abc").build();
        assertThat(cookie.toString()).isEqualTo("jawncookie=\"\\\"abc\";Version=1;Path=/");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void badChar1() {
        new Cookie.Builder("jawncookie", "\n").build().toString();
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void badChar2() {
        new Cookie.Builder("jawncookie", "" + ((char)0x7f)).build().toString();
    }
    
    @Test
    public void session() {
        Cookie cookie = new Cookie.Builder("jawncookie", "value").maxAge(-1).build();
        assertThat(cookie.toString()).isEqualTo("jawncookie=value;Version=1;Path=/");
    }
    
    @Test
    public void emptyValue() {
        Cookie cookie = new Cookie.Builder("jawncookie", "").build();
        assertThat(cookie.toString()).isEqualTo("jawncookie=;Version=1;Path=/");
    }
    
    @Test
    public void httpOnly() {
        Cookie cookie = new Cookie.Builder("jawncookie", "value").httpOnly().build();
        assertThat(cookie.toString()).isEqualTo("jawncookie=value;Version=1;Path=/;HttpOnly");
    }
    
    @Test
    public void secure() {
        Cookie cookie = new Cookie.Builder("jawncookie", "value").secure().build();
        assertThat(cookie.toString()).isEqualTo("jawncookie=value;Version=1;Path=/;Secure");
    }
    
    @Test
    public void maxAgeEpoch() {
        Cookie cookie = new Cookie.Builder("jawncookie", "value").maxAge(0).build();
        assertThat(cookie.toString()).isEqualTo("jawncookie=value;Version=1;Path=/;Max-Age=0;Expires=Thu, 01 Jan 1970 00:00:00 GMT");
    }
    
    @Test
    public void maxAgeOneDay() {
        Cookie cookie = new Cookie.Builder("jawncookie", "value").maxAge(Cookie.ONE_DAY).build();
        assertThat(cookie.toString().startsWith("jawncookie=value;Version=1;Path=/;Max-Age=86400;Expires=")).isTrue();
    }
}
