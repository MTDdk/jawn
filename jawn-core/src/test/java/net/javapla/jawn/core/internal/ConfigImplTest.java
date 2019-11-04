package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.Config;
import net.javapla.jawn.core.util.Modes;

public class ConfigImplTest {
    
    static Config config;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        config = ConfigImpl.parse(Modes.DEV, "jawn_defaults_test.properties");
    }
    

    @Test
    public void parse() {
        assertThat(config).isNotNull();
        assertThat(config.isDev()).isTrue();
        assertThat(config.isTest()).isFalse();
        assertThat(config.isProd()).isFalse();
    }

    @Test
    public void parseWithoutExtention() {
        Config config = ConfigImpl.parse(Modes.DEV, "jawn_defaults_test");
        assertThat(config).isNotNull();
    }
    
    @Test
    public void readValue() {
        assertThat(config.getOptionally("application.charset").isPresent()).isTrue();
        assertThat(config.get("application.charset")).isEqualTo("UTF-8");
        assertThat(config.getOrDie("application.charset")).isEqualTo("UTF-8");
    }
    
    @Test(expected = RuntimeException.class)
    public void failOnMissingKey() {
        config.getOrDie("application.bogus");
    }
    
    @Test
    public void readIntValue() {
        assertThat(config.getIntOptionally("application.someint").isPresent()).isTrue();
        assertThat(config.getIntOptionally("application.someint").get()).isEqualTo(200);
        assertThat(config.getInt("application.someint")).isEqualTo(200);
        assertThat(config.getIntOrDie("application.someint"));
        
        assertThrows(RuntimeException.class, () -> config.getIntOrDie("no"));
    }
    
    @Test
    public void readLongValue() {
        assertThat(config.getLongOptionally("application.somelong").isPresent()).isTrue();
        assertThat(config.getLongOptionally("application.somelong").get()).isEqualTo(4_599_100_100l);
        assertThat(config.getLong("application.somelong")).isEqualTo(4_599_100_100l);
        assertThat(config.getLongOrDie("application.somelong"));
        
        assertThrows(RuntimeException.class, () -> config.getLongOrDie("no"));
    }
    
    @Test
    public void readBoolValue() {
        assertThat(config.getBooleanOptionally("application.somebool").isPresent()).isTrue();
        assertThat(config.getBoolean("application.somebool")).isFalse();
    }
    
    @Test
    public void readDurationValue() {
        assertThat(config.getDuration("application.sometime")).isEqualTo(Duration.ofMinutes(5));
        assertThat(config.getDurationOptionally("application.sometime").get()).isEqualTo(Duration.ofMinutes(5));
        
        assertThat(config.getDuration("application.sometime2")).isEqualTo(Duration.ofSeconds(10));
    }
    
    @Test
    public void readDurationAsAnotherUnit() {
        assertThat(config.getDuration("application.sometime", TimeUnit.SECONDS)).isEqualTo(Duration.ofMinutes(5).getSeconds());
        assertThat(config.getDuration("application.sometime", TimeUnit.MINUTES)).isEqualTo(5);
        assertThat(config.getDuration("application.sometime2", TimeUnit.SECONDS)).isEqualTo(10);
    }
    
    @Test
    public void merge() {
        Config conf1 = ConfigImpl.empty();
        Config conf2 = ConfigImpl.empty();
        
        conf1.set("test", "test");
        conf2.set("test", "overridden");
        conf2.set("extra", "3");
        
        assertThat(conf1.get("extra")).isNull();
        
        ((ConfigImpl) conf1).merge(conf2);
        
        assertThat(conf1.get("test")).isEqualTo("overridden");
        assertThat(conf1.get("extra")).isNotNull();
    }
    
    @Test
    public void mergeWithDifferentModes() {
        Config conf1 = ConfigImpl.empty();
        Config conf2 = ConfigImpl.empty(Modes.TEST);
        
        assertThat(conf1.getMode()).isEqualTo(Modes.DEV);
        
        ((ConfigImpl) conf1).merge(conf2);
        
        assertThat(conf1.getMode()).isEqualTo(Modes.TEST);
    }
    
}
