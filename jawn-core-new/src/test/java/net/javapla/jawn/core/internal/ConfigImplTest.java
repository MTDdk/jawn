package net.javapla.jawn.core.internal;

import static com.google.common.truth.Truth.assertThat;

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
    }
    
    @Test
    public void readBoolValue() {
        assertThat(config.getBooleanOptionally("application.somebool").isPresent()).isTrue();
        assertThat(config.getBoolean("application.somebool")).isFalse();
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
