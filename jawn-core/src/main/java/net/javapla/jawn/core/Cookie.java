package net.javapla.jawn.core;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import net.javapla.jawn.core.util.DateUtil;
import net.javapla.jawn.core.util.URLCodec;

/**
 * We need an internal Cookie representation, as this will make it agnostic to
 * implementation specifics such as a Servlet Cookie.
 * 
 * @author MTD
 */
public class Cookie {
    
    private final String name;
    private final String value;
    private String domain;
    /* private String comment; seems to not be used anywhere */
    
    private String path = "/";
    
    /** True if session cookie is only transmitted via HTTPS */
    private boolean secure = false;
    
    private boolean httpOnly = false;
    
    /** Default is -1, which indicates that the cookie will persist until browser closes */
    private long maxAge = -1;
    
    private SameSite sameSite;
    
    /*private int version = 1; rendered obsolete by RFC 6265*/
    

    public Cookie(final Cookie bob) {
        this.name = bob.name;
        this.value = bob.value;
        this.domain = bob.domain;
        this.path = bob.path;
        this.secure = bob.secure;
        this.httpOnly = bob.httpOnly;
        this.maxAge = bob.maxAge;
    }
    
    public Cookie(String name, /*Nullable*/ String value) {
        if (name == null) throw new IllegalArgumentException(Cookie.class.getSimpleName() + " name = null");
        this.name = name;
        this.value = value;
    }
    

    public String name() {
        return name;
    }

    public String value() {
        return value;
    }

    public /*Nullable*/ String domain() {
        return domain;
    }
    
    public Cookie domain(String domain) {
        this.domain = domain;
        return this;
    }

    public String path() {
        return path;
    }
    
    public Cookie path(String path) {
        this.path = path;
        return this;
    }

    /**
     * Tells if a cookie HTTP only or not.
     *
     * This will only work with Servlet 3
     */
    public boolean httpOnly() {
        return httpOnly;
    }

    /**
     * Sets this cookie to be HTTP only or not
     */
    public Cookie httpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
        return this;
    }
    
    public boolean secure() {
        return secure;
    }
    
    public Cookie secure(boolean secure) {
        if (sameSite != null && sameSite.requiresSecure() && !secure) {
            throw new IllegalArgumentException(
                "With SameSite set to [" + sameSite.value + "]"
              + "the cookie must be set as secure. "
              + "Call Cookie.sameSite(..) with an argument allowing for non-secure cookies");
        }
        this.secure = secure;
        return this;
    }

    public long maxAge() {
        return maxAge;
    }
    
    public Cookie maxAge(long maxAge) {
        if (maxAge >= 0) this.maxAge = maxAge;
        else this.maxAge = -1;
        return this;
    }
    
    public Cookie maxAge(Duration maxAge) {
        return maxAge(maxAge.getSeconds());
    }

    public SameSite sameSite() {
        return sameSite;
    }
    
    public Cookie sameSite(SameSite s) {
        if (s != null && s.requiresSecure() && !this.secure) {
            throw new IllegalArgumentException(
                "With SameSite set to [" + s.value + "]"
              + "the cookie must be set as secure. "
              + "Call Cookie.secure(true) to allow for secure cookies");
        }
        this.sameSite = s;
        return this;
    }


    @Override
    public Cookie clone() {
        return new Cookie(this);
    }

    @Override
    public String toString() {
        StringBuilder bob = new StringBuilder();

        // name = value
        appender(bob, name);
        bob.append("=");
        appender(bob, value);

        // Path
        if (path != null) {
            bob.append(";Path=");
            appender(bob, path);
        }

        // Domain
        if (domain != null) {
            bob.append(";Domain=");
            appender(bob, domain);
        }
        
        // SameSite
        if (sameSite != null) {
            bob.append(";SameSite=");
            appender(bob, sameSite.value);
        }
        
        // Secure
        if (secure) {
            bob.append(";Secure");
        }

        // HttpOnly
        if (httpOnly) {
            bob.append(";HttpOnly");
        }

        // Max-Age
        if (maxAge >= 0) {
            bob.append(";Max-Age=").append(maxAge);

            // Older browsers do not support Max-Age
            Instant instant = Instant.ofEpochMilli(maxAge > 0 ? System.currentTimeMillis() + maxAge * 1000L : 0);
            bob.append(";Expires=").append(DateUtil.toDateString(instant));
        }

        return bob.toString();
    }
    
    static final boolean needQuote(String str) {
        if (str.length() > 1 && str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') {
            return false;
        }

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            
            // TODO is this even necessary anymore?
            if (c < ' ' || c > '~') { // the character is outside simple ASCII
                throw new IllegalArgumentException("Illegal character [" + c + "] found in (" + str + ") at: [" + i + "]");
            }
            
            if (c == '"' || c == ',' || c == ';' || c == '\\' || c == ' ' || c == '\t') { // "\",;\\ \t";
                return true;
            }
        }
        return false;
    }
    
    static final void appender(StringBuilder sb,String str) {
        if (needQuote(str)) {
            sb.append('"');
            for (int i = 0; i < str.length(); ++i) {
                char c = str.charAt(i);
                if (c == '"' || c == '\\') {
                    sb.append('\\');
                }
                sb.append(c);
            }
            sb.append('"');
        } else {
            sb.append(str);
        }
    };
    
    /**
     * The SameSite attribute of the Set-Cookie HTTP response header allows 
     * you to declare if your cookie should be restricted to a first-party or same-site context.
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie/SameSite">
     *      https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Set-Cookie/SameSite</a>
     */
    public static enum SameSite {
        LAX("Lax"),
        STRICT("Strict"),
        NONE("None");
        
        private final String value;
        
        SameSite(String value) {
            this.value = value;
        }
        
        /**
         * Returns the parameter value used in {@code Set-Cookie}.
         */
        public String getValue() {
            return value;
        }
        
        /**
         * Returns whether this value requires the cookie to be flagged as {@code Secure}.
         *
         * @return {@code true} if the cookie should be secure.
         */
        public boolean requiresSecure() {
            return this == NONE;
        }
        
        public static SameSite of(String value) {
            for (var v : values()) {
                if (v.value.equals(value) || v.name().equals(value)) return v;
            }
            throw new IllegalArgumentException("Invalid SameSite value [" + value + "]");
        }
    }
    
    

    /**
     * CookieCodec and CookieCodecTest are imported 
     * (with slight alterations) from Play Framework
     * (originally CookieDataCodec and CookieDataCodecTest respectively).
     * 
     * Enables us to use the same sessions as Play Framework if
     * the secret is the same.
     * 
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
    public static final class CookieCodec {
        
        /**
         * A faster decode than the original from Play Framework, 
         * but still equivalent in output
         * 
         * @param map  the map to decode data into.
         * @param data the data to decode.
         */
        public static void decode(final Map<String, String> map, final String data) {
            //String[] keyValues = StringUtil.split(data, '&');
            split(data, '&',  keyValue -> {
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
         * Helper for {@link #decode(Map, String)}
         * @param data the data to decode
         * @return a map with the decoded data
         */
        public static Map<String, String> decode(final String data) {
            HashMap<String, String> map = new HashMap<>(4);
            decode(map, data);
            return map;
        }

        /**
         * Encode a hash into cookie value, like: <code>k1=v1&amp;...&amp;kn=vn</code>. Also,
         * <code>key</code> and <code>value</code> are encoded using {@link URLCodec}.
         * 
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
        
        /**
         * Splits a string into an array using a provided delimiter.
         * The callback will be invoked once per each found substring
         * 
         * @param input string to split.
         * @param delimiter  delimiter
         * @param callbackPerSubstring called for each split string
         */
        // previously implemented by StringUtil
        private static void split(String input, char delimiter, Consumer<String> callbackPerSubstring) {
            if(input == null) throw new NullPointerException("input cannot be null");
            
            final int len = input.length();
            
            int lastMark = 0;
            for (int i = 0; i < len; i++) {
                if (input.charAt(i) == delimiter) {
                    callbackPerSubstring.accept(input.substring(lastMark, i));
                    lastMark = i + 1;// 1 == delimiter length
                }
            }
            callbackPerSubstring.accept(input.substring(lastMark, len));
        }
    }
}