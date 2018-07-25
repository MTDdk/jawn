package net.javapla.jawn.core.util;

import static org.junit.Assert.assertEquals;

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
    public void decapitalize() {
        assertEquals("alice", StringUtil.decapitalize("Alice"));
        assertEquals("aliceIn", StringUtil.decapitalize("AliceIn"));
        assertEquals("aliceInWonderland", StringUtil.decapitalize("AliceInWonderland"));
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
}
