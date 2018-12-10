package net.javapla.jawn.core;

import static com.google.common.truth.Truth.assertThat;

import org.junit.BeforeClass;
import org.junit.Test;

import net.javapla.jawn.core.util.Modes;

public class ConfigTest {
    
    static Config config;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        config = Config.parse(Modes.DEV, "jawn_defaults_test.properties");
    }
    

    @Test
    public void parse() {
        assertThat(config).isNotNull();
    }

    @Test
    public void parseWithoutExtention() {
        Config config = Config.parse(Modes.DEV, "jawn_defaults_test");
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
}
