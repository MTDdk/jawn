package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import com.google.common.truth.Correspondence;

public class MediaTypeTest {

    @Test
    public void byPath() {
        assertThat(MediaType.byPath("/some/path/image.jpg").get()).isEqualTo(MediaType.valueOf("image/jpg"));
        assertThat(MediaType.byPath("/some/path/image.jpeg").get()).isEqualTo(MediaType.valueOf("image/jpeg"));
        assertThat(MediaType.byPath("/some/path/image.gif").get()).isEqualTo(MediaType.valueOf("image/gif"));
        assertThat(MediaType.byPath("/some/path/image.png").get()).isEqualTo(MediaType.valueOf("image/png"));
        
        assertThat(MediaType.byPath("/videos/video.mp4").get()).isEqualTo(MediaType.valueOf("video/mp4"));
        assertThat(MediaType.byPath("/videos/video.webm").get()).isEqualTo(MediaType.valueOf("video/webm"));
    }
    
    @Test
    public void byPath_fail() {
        assertThat(MediaType.byPath("/nothing/at/all/cookie.dough").isPresent()).isFalse();
        assertThat(MediaType.byPath("/nothing/at/all/cookies").isPresent()).isFalse();
    }
    
    @Test
    public void equals() {
        assertThat(MediaType.FORM.equals(MediaType.valueOf("application/x-www-form-urlencoded"))).isTrue();
        assertThat(MediaType.WILDCARD.equals(new MediaType())).isTrue();
    }
    
    @Test
    public void equals_fail() {
        assertThat(MediaType.FORM.equals(new Object())).isFalse();
    }
    
    @Test
    public void matches() {
        assertThat(MediaType.WILDCARD.matches(MediaType.HTML)).isTrue();
        assertThat(MediaType.WILDCARD.matches(MediaType.JSON_LIKE)).isTrue();
        assertThat(MediaType.WILDCARD.matches(MediaType.valueOf("image/*"))).isTrue();
        
        assertThat(MediaType.JSON_LIKE.matches(MediaType.valueOf("application/json"))).isTrue();
        assertThat(MediaType.XML_LIKE.matches(MediaType.valueOf("application/xml"))).isTrue();
        
        assertThat(new MediaType("application", "*json").matches(MediaType.valueOf("application/vnd.api+json"))).isTrue();
        
        assertThat(MediaType.TEXT.matches(MediaType.valueOf("text/css"))).isTrue();
    }
    
    @Test
    public void matches_fail() {
        assertThat(MediaType.TEXT.matches(MediaType.valueOf("image/png"))).isFalse();
    }
    
    @Test
    public void contentType() {
        MediaType type = MediaType.valueOf("text/html; charset=utf-8");
        assertThat(type.params().get(MediaType.CHARSET_PARAMETER)).isEqualTo("utf-8");
        assertThat(type.type()).isEqualTo("text");
        assertThat(type.subtype()).isEqualTo("html");
    }

    @Test
    public void wildcard() {
        assertThat(MediaType.WILDCARD.isAny()).isTrue();
        
        MediaType wild = new MediaType(null, null, "utf-16");
        assertThat(wild.isAny()).isTrue();
        assertThat(wild.isWildcardType()).isTrue();
        assertThat(wild.isWildcardSubtype()).isTrue();
    }
    
    @Test
    public void list() {
        assertThat(MediaType.parse("*/*,test/some, test/other, text/*,  application/unknown;prop=value, *;charset=windows;prop=value"))
            .comparingElementsUsing(Correspondence.transforming((MediaType type) -> type.toString(), "name"))
            .containsExactly(MediaType.WILDCARD.name(), "test/some", "test/other", MediaType.TEXT.name(), "application/unknown", MediaType.WILDCARD.name());
    }
    
    @Test
    public void incorrects() {
        assertThrows(Up.BadMediaType.class, () -> MediaType.valueOf("*/notreallyusable"));
        assertThrows(Up.BadMediaType.class, () -> MediaType.valueOf("application/image/json"));
    }
}