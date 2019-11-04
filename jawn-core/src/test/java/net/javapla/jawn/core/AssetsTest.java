package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import java.time.Duration;

import org.junit.Test;

public class AssetsTest {

    @Test
    public void maxAge() {
        Assets.Impl impl = new Assets.Impl();
        impl.maxAge();
        assertThat(impl.maxAge).isEqualTo(Assets.ONE_WEEK_SECONDS);
    }
    
    @Test
    public void maxAge_duration() {
        Assets.Impl impl = new Assets.Impl();
        impl.maxAge(Duration.ofMinutes(1));
        assertThat(impl.maxAge).isEqualTo(60);
    }

    @Test
    public void maxAge_string() {
        Assets.Impl impl = new Assets.Impl();
        impl.maxAge("10h");
        assertThat(impl.maxAge).isEqualTo(60 * 60 * 10);
    }
    
    @Test
    public void lastModified() {
        Assets.Impl impl = new Assets.Impl();
        impl.lastModified(true);
        assertThat(impl.lastModified).isTrue();
        
        impl.lastModified(false);
        assertThat(impl.lastModified).isFalse();
    }
    
    @Test
    public void etag() {
        Assets.Impl impl = new Assets.Impl();
        impl.etag(true);
        assertThat(impl.etag).isTrue();
        
        impl.etag(false);
        assertThat(impl.etag).isFalse();
    }
}
