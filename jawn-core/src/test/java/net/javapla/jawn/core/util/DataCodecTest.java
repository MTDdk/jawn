/**
 * Copyright (C) 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.javapla.jawn.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * DataCodec and DataCodecTest are imported 
 * (with slight alterations) from Play Framework
 * (originally CookieDataCodec and CookieDataCodecTest respectively).
 * 
 * Enables us to use the same sessions as Play Framework if
 * the secret is the same.
 * 
 */
public class DataCodecTest {

    @Test
    public void flash_cookies_should_bake_in_a_header_and_value() throws UnsupportedEncodingException {
        final Map<String, String> inMap = new HashMap<String, String>(1);
        inMap.put("a", "b");
        final String data = DataCodec.encode(inMap);
        final Map<String, String> outMap = new HashMap<String, String>(1);
        DataCodec.decode(outMap, data);
        assertEquals(1, outMap.size());
        assertEquals("b", outMap.get("a"));
    }

    @Test
    public void bake_in_multiple_headers_and_values() throws UnsupportedEncodingException {
        final Map<String, String> inMap = new HashMap<String, String>(2);
        inMap.put("a", "b");
        inMap.put("c", "d");
        final String data = DataCodec.encode(inMap);
        final Map<String, String> outMap = new HashMap<String, String>(1);
        DataCodec.decode(outMap, data);
        assertEquals(outMap.size(), 2);
        assertEquals("b", outMap.get("a"));
        assertEquals("d", outMap.get("c"));
    }

    @Test
    public void bake_in_a_header_an_empty_value() throws UnsupportedEncodingException {
        final Map<String, String> inMap = new HashMap<String, String>(1);
        inMap.put("a", "");
        final String data = DataCodec.encode(inMap);
        final Map<String, String> outMap = new HashMap<String, String>(1);
        DataCodec.decode(outMap, data);
        assertEquals(1, outMap.size());
        assertEquals("", outMap.get("a"));
    }

    @Test
    public void bake_in_a_header_a_Unicode_value() throws UnsupportedEncodingException {
        final Map<String, String> inMap = new HashMap<String, String>(1);
        inMap.put("a", "\u0000");
        final String data = DataCodec.encode(inMap);
        final Map<String, String> outMap = new HashMap<String, String>(1);
        DataCodec.decode(outMap, data);
        assertEquals(1, outMap.size(), 1);
        assertEquals("\u0000", outMap.get("a"));
    }

    @Test
    public void bake_in_an_empty_map() throws UnsupportedEncodingException {
        final Map<String, String> inMap = new HashMap<String, String>(0);
        final String data = DataCodec.encode(inMap);
        final Map<String, String> outMap = new HashMap<String, String>(1);
        DataCodec.decode(outMap, data);
        assertEquals(0, outMap.size());
    }

    @Test
    public void encode_values_such_that_no_extra_keys_can_be_created() throws UnsupportedEncodingException {
        final Map<String, String> inMap = new HashMap<String, String>(1);
        inMap.put("a", "b&c=d");
        final String data = DataCodec.encode(inMap);
        final Map<String, String> outMap = new HashMap<String, String>(1);
        DataCodec.decode(outMap, data);
        assertEquals(1, outMap.size());
        assertEquals("b&c=d", outMap.get("a"));
    }

    @Test
    public void specifically_exclude_control_chars() throws UnsupportedEncodingException {
        for (int i = 0; i < 32; ++i) {
            final Map<String, String> inMap = new HashMap<String, String>(1);
            final String s = Arrays.toString(Character.toChars(i));
            inMap.put("a", s);
            final String data = DataCodec.encode(inMap);
            assertFalse(data.contains(s));
            final Map<String, String> outMap = new HashMap<String, String>(1);
            DataCodec.decode(outMap, data);
            assertEquals(1, outMap.size());
            assertEquals(s, outMap.get("a"));
        }
    }

    @Test
    public void specifically_exclude_special_cookie_chars() throws UnsupportedEncodingException {
        final Map<String, String> inMap = new HashMap<String, String>(1);
        inMap.put("a", " \",;\\");
        final String data = DataCodec.encode(inMap);
        assertFalse(data.contains(" "));
        assertFalse(data.contains("\""));
        assertFalse(data.contains(","));
        assertFalse(data.contains(";"));
        assertFalse(data.contains("\\"));
        final Map<String, String> outMap = new HashMap<String, String>(1);
        DataCodec.decode(outMap, data);
        assertEquals(1, outMap.size());
        assertEquals( " \",;\\", outMap.get("a"));
    }

    private String oldEncoder(final Map<String, String> out) throws UnsupportedEncodingException {
        StringBuilder flash = new StringBuilder();
        for (String key : out.keySet()) {
            if (out.get(key) == null) continue;
            flash.append("\u0000");
            flash.append(key);
            flash.append(":");
            flash.append(out.get(key));
            flash.append("\u0000");
        }
        return URLEncoder.encode(flash.toString(), "utf-8");

    }

    @Test
    public void decode_values_of_the_previously_supported_format() throws UnsupportedEncodingException {
        final Map<String, String> inMap = new HashMap<String, String>(2);
        inMap.put("a", "b");
        inMap.put("c", "d");
        final String data = oldEncoder(inMap);
        final Map<String, String> outMap = new HashMap<String, String>(0);
        DataCodec.decode(outMap, data);
        assertEquals(0, outMap.size());
    }

    @Test
    public void decode_values_of_the_previously_supported_format_with_the_new_delimiters_in_them() throws UnsupportedEncodingException {
        final Map<String, String> inMap = new HashMap<String, String>(1);
        inMap.put("a", "b&=");
        final String data = oldEncoder(inMap);
        final Map<String, String> outMap = new HashMap<String, String>(0);
        DataCodec.decode(outMap, data);
        assertEquals(0, outMap.size());
    }

    @Test
    public void decode_values_with_gibberish_in_them() throws UnsupportedEncodingException {
        final String data = "asfjdlkasjdflk";
        final Map<String, String> outMap = new HashMap<String, String>(1);
        DataCodec.decode(outMap, data);
        assertEquals(0, outMap.size());
    }
    
    /*@Test
    public void timing_test() {
        int loops = 30000_000;
        Supplier<Long> time = () -> System.currentTimeMillis();
        final Map<String, String> map1 = new HashMap<String, String>(10);
        final Map<String, String> map2 = new HashMap<String, String>(10);
        final String data = "asfjasdf=dasdfflka&sjdf=lasdfasdfk&&asdddf=&afff";
        
        long t = time.get();
        for (int i = 0; i < loops; i++) {
            DataCodec.decodeFast(map1, data);
        }
        System.out.println((time.get() - t));
        
        t = time.get();
        for (int i = 0; i < loops; i++) {
            DataCodec.decode(map2, data);
        }
        System.out.println((time.get() - t));
        
        System.out.println(map1 + "\n" + map2);
    }*/
}