package net.javapla.jawn.core.cache;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

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
}
