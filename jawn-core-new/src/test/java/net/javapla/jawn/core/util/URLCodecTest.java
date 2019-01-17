package net.javapla.jawn.core.util;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;

import org.junit.Test;


public class URLCodecTest {


	@Test
	public void noEncoding() {
		String str = "";
		assertEquals(str, URLCodec.encode(str, StandardCharsets.UTF_8));
		str = "something";
		assertEquals(str, URLCodec.encode(str, StandardCharsets.UTF_8));
		str = "www.something.com";
		assertEquals(str, URLCodec.encode(str, StandardCharsets.UTF_8));
		str = "www_something_com";
		assertEquals(str, URLCodec.encode(str, StandardCharsets.UTF_8));
		str = "star***";
		assertEquals(str, URLCodec.encode(str, StandardCharsets.UTF_8));
		str = "www_--something--com";
		assertEquals(str, URLCodec.encode(str, StandardCharsets.UTF_8));
		str = "numbers0123456789";
		assertEquals(str, URLCodec.encode(str, StandardCharsets.UTF_8));
	}
	
	@Test
	public void space_is_converted() {
		assertEquals("+spaces+", URLCodec.encode(" spaces ", StandardCharsets.UTF_8));
		assertEquals("++spaces+", URLCodec.encode("  spaces ", StandardCharsets.UTF_8));
		assertEquals("+spa+ces+", URLCodec.encode(" spa ces ", StandardCharsets.UTF_8));
	}
	
	@Test
	public void javadocEncoding() {
		//https://docs.oracle.com/javase/8/docs/api/java/net/URLEncoder.html
		assertEquals("The+string+%C3%BC%40foo-bar", URLCodec.encode("The string ü@foo-bar", StandardCharsets.UTF_8));
	}
	
	@Test
	public void noDecoding() {
		String str = "";
		assertEquals(str, URLCodec.decode(str, StandardCharsets.UTF_8));
		str = "something";
		assertEquals(str, URLCodec.decode(str, StandardCharsets.UTF_8));
		str = "www.something.com";
		assertEquals(str, URLCodec.decode(str, StandardCharsets.UTF_8));
		str = "www_something_com";
		assertEquals(str, URLCodec.decode(str, StandardCharsets.UTF_8));
		str = "star***";
		assertEquals(str, URLCodec.decode(str, StandardCharsets.UTF_8));
		str = "www_--something--com";
		assertEquals(str, URLCodec.decode(str, StandardCharsets.UTF_8));
		str = "numbers0123456789";
		assertEquals(str, URLCodec.decode(str, StandardCharsets.UTF_8));
	}
	
	@Test
	public void javadocDecoding() {
		assertEquals("The string ü@foo-bar", URLCodec.decode("The+string+%C3%BC%40foo-bar", StandardCharsets.UTF_8));
	}

}
