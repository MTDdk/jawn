package net.javapla.jawn.core.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

public class CollectionUtilTest {

    @Test
    public void collection_should_mapToKeyValues() {
        String key1 = "key1",
               key2 = "key2";
        
        String value1 = "value1",
               value2 = "value2";
        
        Map<String, String> map = CollectionUtil.map(key1,value1,key2,value2);
        
        assertThat(map.keySet()).containsExactly(key1, key2);
        assertThat(map.values()).containsExactly(value1, value2);
    }
    
    @Test
    public void unevenCollection_should_throw() {
        try {
            CollectionUtil.map("","","","","");
            fail();
        } catch (IllegalArgumentException e) {}
        
        try {
            CollectionUtil.map("");
            fail();
        } catch (IllegalArgumentException e) {}
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void nullCollection_should_throw() {
        CollectionUtil.map((Object[])null);
    }

}
