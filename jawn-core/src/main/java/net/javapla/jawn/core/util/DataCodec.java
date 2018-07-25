/*
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

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * DataCodec and DataCodecTest are imported 
 * (with slight alterations) from Play Framework
 * (originally CookieDataCodec and CookieDataCodecTest respectively).
 * 
 * Enables us to use the same sessions as Play Framework if
 * the secret is the same.
 * 
 */
public final class DataCodec {
    
    /**
     * A faster decode than the original from Play Framework, 
     * but still equivalent in output
     * 
     * @param map  the map to decode data into.
     * @param data the data to decode.
     */
    public static void decode(final Map<String, String> map, final String data) {
        //String[] keyValues = StringUtil.split(data, '&');
        StringUtil.split(data, '&',  keyValue -> {
            final int indexOfSeperator = keyValue.indexOf('=');
            
            if (indexOfSeperator > -1) {
                if (indexOfSeperator == keyValue.length() - 1) { // The '=' is at the end of the string - this counts as an unsigned value
                    map.put(URLCodec.decode(keyValue.substring(0, indexOfSeperator), StandardCharsets.UTF_8), "");
                } else {  
                    final String first  = keyValue.substring(0, indexOfSeperator),
                                 second = keyValue.substring(indexOfSeperator + 1);
                 
                    map.put(URLCodec.decode(first, StandardCharsets.UTF_8), URLCodec.decode(second, StandardCharsets.UTF_8));
                }
            }
        });
    }

    /**
     * @param map the data to encode.
     * @return the encoded data.
     */
    public static String encode(final Map<String, String> map) {
        if (map.isEmpty()) return "";
        
        final StringBuilder data = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                data.append(URLCodec.encode(entry.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLCodec.encode(entry.getValue(), StandardCharsets.UTF_8))
                    .append('&');
            }
        }
        data.deleteCharAt(data.length()-1);// remove last '&'
        return data.toString();
    }

    /**
     * Constant time for same length String comparison, to prevent timing attacks
     */
    public static boolean safeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        } else {
            char equal = 0;
            for (int i = 0; i < a.length(); i++) {
                equal |= a.charAt(i) ^ b.charAt(i);
            }
            return equal == 0;
        }
    }
}
