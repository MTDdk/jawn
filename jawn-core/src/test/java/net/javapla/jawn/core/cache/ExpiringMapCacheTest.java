package net.javapla.jawn.core.cache;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExpiringMapCacheTest {
    
    ExpiringMapCache cache;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {
        cache = new ExpiringMapCache();
    }

    @After
    public void tearDown() throws Exception {}
    
    @Test
    public void defaultvalues() {
        int TEN_MINUTES = 60 * 10;
        assertThat(cache.getDefaultCacheExpiration(), is(TEN_MINUTES));
    }

    @Test
    public void adding_shouldBe_retrievable() {
        String 
            key = "key",
            value = "value",
            key2 = "key2",
            value2 = "value2";
        
        cache.add(key, value);
        cache.add(key2, value2);
        
        assertThat(cache.get(key), is(value));
        assertThat(cache.get(key2), is(value2));
    }
    
    @Test
    public void alteration_not_throughAdd() {
        String
            key = "key",
            value ="value",
            fault = "fault";
        
        cache.add(key, value);
        assertThat(cache.get(key), is(value));
        
        cache.add(key, fault);
        assertThat(cache.get(key), not(fault));
        assertThat(cache.get(key), is(value));
    }
    
    @Test
    public void alteration_shouldBe_bySetting() {
        String
            key = "key",
            value ="value",
            alter = "alter";
        
        cache.add(key, value);
        
        cache.set(key, alter);
        assertThat(cache.get(key), not(value));
        assertThat(cache.get(key), is(alter));
        

        cache.add(key, value, 444);
        assertThat(cache.get(key), not(value));
    }
    
    @Test
    public void defaultExpiration_on_keys() {
        String 
            key = "key",
            value = "value",
            key2 = "key2",
            value2 = "value2";
    
        cache.add(key, value);
        cache.add(key2, value2);
        
        assertThat(cache.getExpiration(key), is(cache.getDefaultCacheExpiration()));
        assertThat(cache.getExpiration(key2), is(cache.getDefaultCacheExpiration()));
    }
    
    @Test
    public void expirationOnKeys_canBe_set() {
        String 
            key = "key",
            value = "value",
            key2 = "key2",
            value2 = "value2",
            key3 = "key3",
            value3 = "value3";
        
        int keySeconds = 10,
            key2Seconds = 73,
            key3Seconds = 22;
        
        cache.add(key, value, keySeconds);
        cache.add(key2, value2, key2Seconds);
        cache.add(key3, value3);
        
        assertThat(cache.getExpiration(key), is(keySeconds));
        assertThat(cache.getExpiration(key2), is(key2Seconds));
        assertThat(cache.getExpiration(key3), is(cache.getDefaultCacheExpiration()));
        
        cache.setExpiration(key3, key3Seconds);
        assertThat(cache.getExpiration(key3), is(key3Seconds));
        assertThat(cache.get(key3), is(value3));
    }
    
    @Test
    public void valueAndExpiration_couldBe_set() {
        String 
            key = "key",
            value = "value",
            value2 = "value2";
        
        int keySeconds = 10,
            key2Seconds = 73;
        
        cache.add(key, value, keySeconds);
        
        assertThat(cache.getExpiration(key), is(keySeconds));
        
        cache.set(key, value2, key2Seconds);
        
        assertThat(cache.get(key), is(value2));
        assertThat(cache.getExpiration(key), is(key2Seconds));
    }
    
    @Test
    public void cache_should_holdGenerics() {
        String key = "keyz", value = "valuez", key2 = "key2", key3 = "key3";
        Integer value2 = 73;
        Boolean value3 = false;
        
        cache.add(key, value);
        cache.add(key2, value2);
        cache.add(key3, value3);
        
        assertThat(cache.get(key), is(instanceOf(String.class)));
        assertThat(cache.get(key2), is(instanceOf(Integer.class)));
        assertThat(cache.get(key3), is(instanceOf(Boolean.class)));
    }
    
    @Test
    public void computeIfAbsent() {
        String key = "key", key2 = "key2";
        
        cache.computeIfAbsent(key, () -> {
            int i = 4;
            return i + 6;
        });
        
        assertThat(cache.get(key), is(10));
        
        cache.computeIfAbsent(key2, (k) -> k.toUpperCase());
        assertThat(cache.get(key2), is(key2.toUpperCase()));
    }
    
    @Test
    public void computeIfAbsent_with_expiration() {
        String key = "key", key2 = "key2";
        
        cache.computeIfAbsent(key, 33, () -> {
            int i = 4;
            return i + 6;
        });
        
        assertThat(cache.getExpiration(key), is(33));
        
        cache.computeIfAbsent(key2, 44, (k) -> k.toUpperCase());
        assertThat(cache.getExpiration(key2), is(44));
    }
    
    @Test
    public void compute_shouldNot_overwrite() {
        String key = "key";
        
        cache.computeIfAbsent(key, 44, (k) -> k.toUpperCase());
        
        assertThat(cache.computeIfAbsent(key, 55, (k) -> "new value"), is(key.toUpperCase()));
        assertThat(cache.computeIfAbsent(key, 55, () -> "new value"), is(key.toUpperCase()));
        
        assertThat(cache.computeIfAbsent("key2", 55, (k) -> null), is(nullValue()));
        assertThat(cache.computeIfAbsent("key3", 55, () -> null), is(nullValue()));
    }
    
    @Test
    public void deletions() {
        String 
            key = "key",
            value = "value",
            key2 = "key2",
            value2 = "value2",
            key3 = "key3",
            value3 = "value3";
        
        cache.add(key, value);
        cache.add(key2, value2);
        cache.add(key3, value3);
        
        assertTrue(cache.isSet(key));
        cache.delete(key);
        assertFalse(cache.isSet(key));
        
        
        assertTrue(cache.isSet(key2));
        assertTrue(cache.isSet(key3));
        cache.clear();
        assertFalse(cache.isSet(key2));
        assertFalse(cache.isSet(key3));
    }
    
    @Test
    public void key_mustNot_beNull() {
        try {
            cache.add(null, "something");
            fail();
        } catch (IllegalArgumentException correct) {}
        
        try {
            cache.add("", "something", 100);
            fail();
        } catch (IllegalArgumentException correct) {}
        
        try {
            cache.set(null, "something");
            fail();
        } catch (IllegalArgumentException correct) {}
        
        try {
            cache.computeIfAbsent(null, () -> "nothing");
            fail();
        } catch (IllegalArgumentException correct) {}
        
        try {
            cache.computeIfAbsent(null, (key) -> "nothing");
            fail();
        } catch (IllegalArgumentException correct) {}
        
        try {
            cache.delete(null);
            fail();
        } catch (IllegalArgumentException correct) {}
        
        try {
            cache.delete("");
            fail();
        } catch (IllegalArgumentException correct) {}
    }
}
