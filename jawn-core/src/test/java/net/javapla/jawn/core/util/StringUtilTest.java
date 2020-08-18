package net.javapla.jawn.core.util;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;


public class StringUtilTest {

    @Test
    public void underscore() {
        assertEquals("alice_in_wonderland", StringUtil.underscore("AliceInWonderland"));
        assertEquals("alice_in_wonderland", StringUtil.underscore("aliceInWonderland"));
        assertEquals("alice_in_wonder_land", StringUtil.underscore("aliceInWonderLand"));
        assertEquals("alice_in_wonder_lan_d", StringUtil.underscore("aliceInWonderLanD"));
        
        assertEquals("favicon.ico", StringUtil.underscore("favicon.ico"));
    }
    
    @Test
    public void camelcase() {
        assertEquals("aliceInWonderland", StringUtil.camelize("alice_in_wonderland", false));
        assertEquals("AliceInWonderland", StringUtil.camelize("alice_in_wonderland"));
        assertEquals("AliceInWonderLand", StringUtil.camelize("alice_in_wonder_land"));
        assertEquals("AliceInWonderLanD", StringUtil.camelize("alice_in_wonder_lan_d"));
        
        assertEquals("favicon.ico", StringUtil.camelize("favicon.ico", false));
    }

    @Test
    public void decapitalize() {
        assertEquals("alice", StringUtil.decapitalize("Alice"));
        assertEquals("aliceIn", StringUtil.decapitalize("AliceIn"));
        assertEquals("aliceInWonderland", StringUtil.decapitalize("AliceInWonderland"));
    }
    
    @Test
    public void capitalize() {
        assertEquals("Alice", StringUtil.capitalize("alice"));
        assertEquals("AliceIn", StringUtil.capitalize("aliceIn"));
        assertEquals("AliceInWonderland", StringUtil.capitalize("aliceInWonderland"));
    }
 
    @Test
    public void splitWithCallback() {
        String input = "qwerty&asdfgh&zxcvbn";
        ArrayList<String> arr = new ArrayList<>();
        
        StringUtil.split(input, '&', arr::add);
        assertEquals(Arrays.toString(StringUtil.split(input, '&')), arr.toString());
        arr.clear();
        
        input = "+102394+&kmadælæ&&asdfas&";
        StringUtil.split(input, '&', arr::add);
        assertEquals(Arrays.toString(StringUtil.split(input, '&')), arr.toString());
        arr.clear();
    }
    
    @Test
    public void blank() {
        assertTrue(StringUtil.blank(""));
        assertTrue(StringUtil.blank(" "));
        assertFalse(StringUtil.blank("false"));
    }
    
    @Test
    public void hex() {
        assertThat(StringUtil.hex("cookie".getBytes(StandardCharsets.UTF_8))).isEqualTo("636f6f6b6965");
    }
}
